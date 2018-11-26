/*
 * Copyright 2018 LINE Corporation
 *
 * LINE Corporation licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.linecorp.clova.extension.boot.handler;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.core.MethodParameter;
import org.springframework.util.Assert;
import org.springframework.validation.BindException;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.linecorp.clova.extension.boot.exception.CEKHandlerInterceptException;
import com.linecorp.clova.extension.boot.exception.RequestHandlerNotFoundException;
import com.linecorp.clova.extension.boot.exception.TooManyMatchedRequestHandlersException;
import com.linecorp.clova.extension.boot.handler.interceptor.CEKHandlerInterceptor;
import com.linecorp.clova.extension.boot.message.context.SystemContext;
import com.linecorp.clova.extension.boot.message.request.CEKRequest;
import com.linecorp.clova.extension.boot.message.request.CEKRequestMessage;
import com.linecorp.clova.extension.boot.message.request.EventRequest;
import com.linecorp.clova.extension.boot.message.request.RequestType;
import com.linecorp.clova.extension.boot.message.response.CEKResponse;
import com.linecorp.clova.extension.boot.message.response.CEKResponseMessage;
import com.linecorp.clova.extension.boot.session.SessionHolder;
import com.linecorp.clova.extension.boot.util.RequestUtils;
import com.linecorp.clova.extension.boot.verifier.CEKRequestVerifier;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * A {@link CEKRequestProcessor} to dispatch the received CEK request to the appropriate Handler method.
 */
@Slf4j
@RequiredArgsConstructor
public class CEKRequestHandlerDispatcher implements CEKRequestProcessor {

    private final CEKRequestMappingHandlerMapping handlerMapping;
    private final SmartValidator validator;
    private final ObjectMapper objectMapper;

    @Setter
    private List<CEKRequestVerifier> requestVerifiers = Collections.emptyList();
    @Setter
    private Map<String, CEKHandlerInterceptor> handlerInterceptorMap = Collections.emptyMap();

    /**
     * Parses the CEK request, extracts the appropriate HandlerMethod, and executes it.
     *
     * @param request        {@link HttpServletRequest}
     * @param requestMessage {@link CEKRequestMessage}
     * @return {@link CEKResponseMessage}
     */
    @Override
    @SuppressWarnings({ "unchecked", "rawTypes" })
    public CEKResponseMessage process(@NonNull HttpServletRequest request, CEKRequestMessage requestMessage)
            throws Throwable {
        RequestType requestType = getRequestType(requestMessage.getRequest());
        String requestName = Optional.ofNullable(requestMessage.getRequest())
                                     .map(CEKRequest::getName)
                                     .orElse(null);
        log.info("request:[type:{}, name:{}]", requestType, requestName);

        RequestUtils.setRequestType(request, requestType);
        RequestUtils.setRequestName(request, requestName);

        if (requestType == RequestType.EVENT) {
            EventRequest eventRequest = (EventRequest) requestMessage.getRequest();
            RequestUtils.setRequestId(request, eventRequest.getRequestId());
        }

        SystemContext system = objectMapper.convertValue(requestMessage.getContext().get("System"),
                                                         SystemContext.class);

        String requestJson = RequestUtils.getRequestBodyJson(request);
        requestVerifiers.forEach(verifier -> verifier.verify(request, requestMessage, requestJson, system));

        CEKHandlerMethod handlerMethod = extractHandlerMethod(request, requestMessage, system);
        if (handlerMethod == null) {
            throw new RequestHandlerNotFoundException(
                    getRequestType(requestMessage.getRequest()),
                    requestMessage.getRequest().getName());
        }

        requestMessage.getSession().toReadOnly();
        Object[] args = handlerMethod.resolveArguments(requestMessage);

        SessionHolder sessionHolder = Arrays.stream(args)
                                            .filter(arg -> arg instanceof SessionHolder)
                                            .map(SessionHolder.class::cast)
                                            .findFirst()
                                            .orElseGet(() -> new SessionHolder(
                                                    objectMapper, requestMessage.getSession()));

        return invokeWithInterceptors(request, requestMessage, sessionHolder, handlerMethod, args);
    }

    CEKResponseMessage invokeWithInterceptors(HttpServletRequest request, CEKRequestMessage requestMessage,
                                              SessionHolder sessionHolder,
                                              CEKHandlerMethod handlerMethod, Object[] args) throws Throwable {
        Set<String> shouldNotInterceptTargets = new HashSet<>();

        this.handlerInterceptorMap.forEach((beanName, interceptor) -> {
            if (interceptor.shouldNotIntercept(request, requestMessage, handlerMethod, args)) {
                shouldNotInterceptTargets.add(beanName);
            }
        });

        CEKResponseMessage responseMessage = null;
        Throwable shouldBeThrown = null;

        try {
            for (Entry<String, CEKHandlerInterceptor> interceptorEntry : this.handlerInterceptorMap
                    .entrySet()) {
                String beanName = interceptorEntry.getKey();
                CEKHandlerInterceptor interceptor = interceptorEntry.getValue();
                try {
                    if (shouldNotInterceptTargets.contains(beanName)) {
                        continue;
                    }
                    if (shouldBeThrown != null) {
                        shouldNotInterceptTargets.add(beanName);
                        continue;
                    }
                    interceptor.preHandle(request, requestMessage, handlerMethod, args);
                } catch (CEKHandlerInterceptException e) {
                    if (shouldBeThrown == null) {
                        shouldBeThrown = e;
                    }
                } catch (Exception e) {
                    if (shouldBeThrown == null) {
                        shouldBeThrown = new CEKHandlerInterceptException(handlerMethod, args, e);
                    }
                }
            }

            if (shouldBeThrown == null) {
                validate(handlerMethod.getMethodParams(), args);
                Object result = handlerMethod.invoke(args);

                if (result == null) {
                    throw new NullPointerException(
                            "Handler method should not return null. "
                            + "[method:" + handlerMethod.getMethod() + "]");
                }

                Assert.isInstanceOf(CEKResponse.class, result);

                responseMessage = new CEKResponseMessage((CEKResponse) result);
                Boolean shouldEndSession = responseMessage.getResponse().getShouldEndSession();
                if (shouldEndSession != null && !shouldEndSession) {
                    responseMessage.setSessionAttributes(sessionHolder.getSessionAttributes());
                }
            }

        } catch (Throwable t) {
            shouldBeThrown = t;
        }

        for (Entry<String, CEKHandlerInterceptor> interceptorEntry : this.handlerInterceptorMap.entrySet()) {
            try {
                String beanName = interceptorEntry.getKey();
                CEKHandlerInterceptor interceptor = interceptorEntry.getValue();
                if (shouldNotInterceptTargets.contains(beanName)) {
                    continue;
                }
                interceptor.postHandle(request, requestMessage, responseMessage, handlerMethod, args);
            } catch (CEKHandlerInterceptException e) {
                if (shouldBeThrown == null) {
                    shouldBeThrown = e;
                }
            } catch (Exception e) {
                if (shouldBeThrown == null) {
                    shouldBeThrown = new CEKHandlerInterceptException(handlerMethod, args, e);
                }
            }
        }

        if (shouldBeThrown != null) {
            throw shouldBeThrown;
        }

        return responseMessage;
    }

    private void validate(List<MethodParameter> methodParams, Object[] args) throws BindException {
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg == null) {
                continue;
            }

            if (arg instanceof Optional) {
                arg = ((Optional<?>) arg).orElse(null);
                if (arg == null) {
                    continue;
                }
            }

            MethodParameter methodParam = methodParams.get(i);
            Valid valid = methodParam.getParameterAnnotation(Valid.class);
            Validated validated = methodParam.getParameterAnnotation(Validated.class);
            if (valid == null && validated == null) {
                continue;
            }

            String paramName = methodParam.getParameterName();
            Assert.notNull(paramName, "paramName should not be null.");

            BindException bindException = new BindException(arg, paramName);

            if (valid != null) {
                validator.validate(arg, bindException);
            } else {
                Class<?>[] hints = validated.value();
                if (hints.length > 0) {
                    validator.validate(arg, bindException, (Object[]) hints);
                } else {
                    validator.validate(arg, bindException);
                }
            }

            if (bindException.hasErrors()) {
                throw bindException;
            }
        }

    }

    private RequestType getRequestType(CEKRequest request) {
        if (request == null) {
            return null;
        }
        return request.getType();
    }

    private CEKHandlerMethod extractHandlerMethod(HttpServletRequest request, CEKRequestMessage requestMessage,
                                                  SystemContext system) {
        List<CEKHandlerMethod> handlerMethods =
                this.handlerMapping.getHandlerMethodMap()
                                   .getOrDefault(requestMessage.getRequest().getType(),
                                                 Collections.emptyMap())
                                   .getOrDefault(createKey(requestMessage.getRequest()),
                                                 Collections.emptyList())
                                   .stream()
                                   .filter(handlerMethod ->
                                                   handlerMethod.getCompositeMatcher()
                                                                .match(request, requestMessage, system))
                                   .sorted()
                                   .collect(Collectors.toList());

        // Not found handler method
        if (handlerMethods.isEmpty()) {
            throw new RequestHandlerNotFoundException(
                    getRequestType(requestMessage.getRequest()),
                    requestMessage.getRequest().getName());
        }
        // Found only one method
        if (handlerMethods.size() == 1) {
            return handlerMethods.get(0);
        }
        // Found more than two methods
        CEKHandlerMethod firstMethod = handlerMethods.get(0);
        CEKHandlerMethod secondMethod = handlerMethods.get(1);
        if (firstMethod.compareTo(secondMethod) != 0) {
            return firstMethod;
        }
        // Cannot select method to call.
        throw new TooManyMatchedRequestHandlersException(
                requestMessage.getRequest().getType(),
                requestMessage.getRequest().getName(),
                handlerMethods);
    }

    private static CEKRequestKey createKey(CEKRequest request) {
        CEKRequestKey requestKey = new CEKRequestKey();
        requestKey.setKey(request.getName());
        return requestKey;
    }

}
