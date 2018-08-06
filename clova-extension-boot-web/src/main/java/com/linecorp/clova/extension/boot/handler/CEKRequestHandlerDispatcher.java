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

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.linecorp.clova.extension.boot.exception.CEKHandlerInterceptException;
import com.linecorp.clova.extension.boot.exception.EmptyRequestMessageException;
import com.linecorp.clova.extension.boot.exception.InvalidApplicationParameterException;
import com.linecorp.clova.extension.boot.exception.MissingContextException;
import com.linecorp.clova.extension.boot.exception.MissingRequiredParamException;
import com.linecorp.clova.extension.boot.exception.MissingSessionAttributeException;
import com.linecorp.clova.extension.boot.exception.MissingSlotException;
import com.linecorp.clova.extension.boot.exception.RequestHandlerNotFoundException;
import com.linecorp.clova.extension.boot.exception.TooManyMatchedRequestHandlersException;
import com.linecorp.clova.extension.boot.exception.UnsupportedHandlerArgumentException;
import com.linecorp.clova.extension.boot.handler.annnotation.CEKRequestParam;
import com.linecorp.clova.extension.boot.handler.annnotation.ContextValue;
import com.linecorp.clova.extension.boot.handler.annnotation.SessionValue;
import com.linecorp.clova.extension.boot.handler.annnotation.SlotValue;
import com.linecorp.clova.extension.boot.handler.interceptor.CEKHandlerInterceptor;
import com.linecorp.clova.extension.boot.message.context.ContextProperty;
import com.linecorp.clova.extension.boot.message.context.SystemContext;
import com.linecorp.clova.extension.boot.message.payload.Payload;
import com.linecorp.clova.extension.boot.message.request.CEKRequest;
import com.linecorp.clova.extension.boot.message.request.CEKRequestMessage;
import com.linecorp.clova.extension.boot.message.request.EventRequest;
import com.linecorp.clova.extension.boot.message.request.IntentRequest;
import com.linecorp.clova.extension.boot.message.request.IntentRequest.Intent.Slot;
import com.linecorp.clova.extension.boot.message.request.RequestType;
import com.linecorp.clova.extension.boot.message.response.CEKResponse;
import com.linecorp.clova.extension.boot.message.response.CEKResponseMessage;
import com.linecorp.clova.extension.boot.session.SessionHolder;
import com.linecorp.clova.extension.boot.util.RequestUtils;
import com.linecorp.clova.extension.boot.util.StringUtils;
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

    private static final Map<Predicate<String>, UnaryOperator<String>> CAMEL_CONVERTERS_BY_CONDITION;

    static {
        CAMEL_CONVERTERS_BY_CONDITION = new HashMap<>();
        CAMEL_CONVERTERS_BY_CONDITION.put(StringUtils::isPascalCase, StringUtils::pascalToCamel);
        CAMEL_CONVERTERS_BY_CONDITION.put(StringUtils::isLowerSnakeCase, StringUtils::lowerSnakeToCamel);
        CAMEL_CONVERTERS_BY_CONDITION.put(StringUtils::isUpperSnakeCase, StringUtils::upperSnakeToCamel);
        CAMEL_CONVERTERS_BY_CONDITION.put(StringUtils::isLowerKebabCase, StringUtils::lowerKebabToCamel);
        CAMEL_CONVERTERS_BY_CONDITION.put(StringUtils::isUpperKebabCase, StringUtils::upperKebabToCamel);
    }

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
     * @param request {@link HttpServletRequest}
     * @param requestMessage {@link CEKRequestMessage}
     *
     * @return {@link CEKResponseMessage}
     */
    @Override
    @SuppressWarnings({ "unchecked", "rawTypes" })
    public CEKResponseMessage process(@NonNull HttpServletRequest request, CEKRequestMessage requestMessage)
            throws Throwable {
        if (requestMessage == null) {
            throw new EmptyRequestMessageException();
        }

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

        Map<String, Object> contexts = requestMessage.getContext();

        requestMessage.getSession().toReadOnly();
        SessionHolder sessionHolder = new SessionHolder(objectMapper, requestMessage.getSession());

        Object[] args =
                handlerMethod.getMethodParams().stream()
                             .map(methodParam -> {
                                 if (CEKRequestMessage.Session.class == methodParam
                                         .getParameterType()) {
                                     return requestMessage.getSession();
                                 }
                                 if (SessionHolder.class == methodParam.getParameterType()) {
                                     return sessionHolder;
                                 }
                                 if (canConvert(SystemContext.class, methodParam)) {
                                     if (Optional.class.isAssignableFrom(methodParam.getParameterType())) {
                                         return Optional.of(system);
                                     }
                                     return system;
                                 }
                                 if (methodParam.hasParameterAnnotation(ContextValue.class)) {
                                     if (!canConvert(ContextProperty.class, methodParam)) {
                                         throw new UnsupportedHandlerArgumentException(
                                                 methodParam,
                                                 "Parameter annotated with ContextValue should be"
                                                 + " implemented ContextProperty."); //TODO: Extract mapper func
                                     }
                                     return extractAndConvertMethodParam(contexts, methodParam,
                                                                         MissingContextException::new);
                                 }
                                 if (canConvert(ContextProperty.class, methodParam)) {
                                     return extractAndConvertMethodParam(contexts, methodParam,
                                                                         MissingContextException::new);
                                 }
                                 if (methodParam.hasParameterAnnotation(SessionValue.class)) {
                                     return extractAndConvertMethodParam(
                                             requestMessage.getSession().getSessionAttributes(),
                                             methodParam,
                                             MissingSessionAttributeException::new);
                                 }
                                 switch (requestMessage.getRequest().getType()) {
                                     case INTENT:
                                         IntentRequest intentRequest =
                                                 (IntentRequest) requestMessage.getRequest();
                                         if (methodParam.hasParameterAnnotation(SlotValue.class)) {
                                             return extractAndConvertMethodParam(
                                                     intentRequest.getIntent().getSlots(),
                                                     (slots, name) -> Optional.ofNullable(slots)
                                                                              .map(s -> s.get(name))
                                                                              .orElse(null),
                                                     methodParam, MissingSlotException::new);
                                         }
                                         break;

                                     case EVENT:
                                         EventRequest eventRequest =
                                                 (EventRequest) requestMessage.getRequest();
                                         if (Payload.class.isAssignableFrom(
                                                 methodParam.getParameterType())) {
                                             Payload payload = eventRequest.getEvent().getPayload();
                                             return convertValue(payload, methodParam);
                                         }
                                         break;
                                 }
                                 throw new UnsupportedHandlerArgumentException(methodParam);
                             })
                             .toArray();

        return invokeWithInterceptors(request, requestMessage, sessionHolder, handlerMethod, args);
    }

    <V> Object extractAndConvertMethodParam(Map<String, V> params, @NonNull MethodParameter methodParam,
                                            @NonNull ParamNameToMissingExceptionConverter throwerIfMissing) {
        return extractAndConvertMethodParam(params, Map::get, methodParam, throwerIfMissing);
    }

    <V> Object extractAndConvertMethodParam(Map<String, V> params, @NonNull ParamAccessor<V> paramAccessor,
                                            @NonNull MethodParameter methodParam,
                                            @NonNull ParamNameToMissingExceptionConverter throwerIfMissing) {
        Map<String, V> camelSupported = supportAlsoCamel(params);

        Annotation annotation = Arrays.stream(methodParam.getParameterAnnotations())
                                      .filter(a -> a.annotationType()
                                                    .isAnnotationPresent(CEKRequestParam.class))
                                      .findFirst()
                                      .orElse(null);

        String name = Optional.ofNullable(annotation)
                              .map(a -> (String) AnnotationUtils.getValue(a))
                              .filter(StringUtils::isNotBlank)
                              .orElse(methodParam.getParameterName());

        Object paramValue = paramAccessor.access(camelSupported, name);

        if (Optional.class.isAssignableFrom(methodParam.getParameterType())) {
            if (paramValue == null) {
                return Optional.empty();
            }
            return convertValue(paramValue, methodParam);
        }

        if (CollectionUtils.isEmpty(params) || paramValue == null) {
            boolean required = true;
            if (annotation != null) {
                required = Optional.ofNullable(AnnotationUtils.getValue(annotation, "required"))
                                   .map(val -> (boolean) val)
                                   .orElse(false);
            }
            if (required) {
                throw throwerIfMissing.convert(name);
            } else {
                return null;
            }
        }

        return convertValue(paramValue, methodParam);
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

    private boolean canConvert(Type allowedType, MethodParameter methodParam) {
        ResolvableType allowedResolvableType = ResolvableType.forType(allowedType);
        ResolvableType paramResolvableType = ResolvableType.forMethodParameter(methodParam);

        if (Optional.class.isAssignableFrom(paramResolvableType.resolve())) {
            return allowedResolvableType.isAssignableFrom(paramResolvableType.getGeneric());
        }

        return allowedResolvableType.isAssignableFrom(paramResolvableType);
    }

    private <V> Map<String, V> supportAlsoCamel(Map<String, V> params) {
        if (params == null || params.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, V> newParams = new HashMap<>(params);
        params.forEach((key, value) -> {
            CAMEL_CONVERTERS_BY_CONDITION.entrySet().stream()
                                         .filter(converterWithCondition ->
                                                         converterWithCondition.getKey().test(key))
                                         .findFirst()
                                         .map(converterWithCondition ->
                                                      converterWithCondition.getValue().apply(key))
                                         .ifPresent(camelKey -> newParams.put(camelKey, value));
        });
        return newParams;
    }

    private Object convertValue(Object object, MethodParameter methodParam) {
        Object value = object;
        try {
            if (object instanceof Slot) {
                Slot slot = (Slot) object;
                value = Optional.ofNullable(slot.convertValueAsType())
                                .orElseGet(slot::getValue);
            }
            return objectMapper.convertValue(value, objectMapper.getTypeFactory().constructType(
                    methodParam.getGenericParameterType()));
        } catch (Exception e) {
            throw new InvalidApplicationParameterException(
                    "Failed to mapping. "
                    + "[" + value + " -> " + StringUtils.methodParamToString(methodParam) + "]", e);
        }
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
                                   .filter(handlerMethod -> {
                                       if (handlerMethod.getConditionMatchers().isEmpty()) {
                                           // Include no condition handler method always.
                                           return true;
                                       }
                                       return handlerMethod
                                               .getConditionMatchers().stream()
                                               .anyMatch(conditionMatcher ->
                                                                 conditionMatcher.match(request,
                                                                                        requestMessage,
                                                                                        system));
                                   })
                                   .collect(Collectors.toList());

        // Not found handler method
        if (handlerMethods.isEmpty()) {
            throw new RequestHandlerNotFoundException(
                    getRequestType(requestMessage.getRequest()),
                    requestMessage.getRequest().getName());
        }
        // No condition method & condition matched method
        if (handlerMethods.size() == 2) {
            boolean hasCondition1 = !handlerMethods.get(0).getConditionMatchers().isEmpty();
            boolean hasCondition2 = !handlerMethods.get(1).getConditionMatchers().isEmpty();
            if (hasCondition1 && !hasCondition2) {
                return handlerMethods.get(0);
            }
            if (!hasCondition1 && hasCondition2) {
                return handlerMethods.get(1);
            }
            // If both of the two have condition or don't have any conditions,
            // TooManyMatchedRequestHandlersException is thrown in next block.
        }
        if (handlerMethods.size() > 1) {
            throw new TooManyMatchedRequestHandlersException(
                    requestMessage.getRequest().getType(),
                    requestMessage.getRequest().getName(),
                    handlerMethods);
        }

        // No condition handler method.
        return handlerMethods.get(0);
    }

    private static CEKRequestKey createKey(CEKRequest request) {
        CEKRequestKey requestKey = new CEKRequestKey();
        requestKey.setKey(request.getName());

        if (request instanceof IntentRequest) {
            IntentRequest intentRequest = (IntentRequest) request;
            if (intentRequest.getIntent().getSlots() != null
                && !intentRequest.getIntent().getSlots().isEmpty()) {
                Set<String> paramNameAndTypes =
                        intentRequest.getIntent().getSlots().entrySet().stream()
                                     .filter(entry -> entry.getValue().getValueType() != null)
                                     .map(entry -> entry.getKey() + "@"
                                                   + entry.getValue().getValueType().name().toLowerCase())
                                     .collect(Collectors.toSet());
                requestKey.setParamNameAndTypes(paramNameAndTypes);
            }
        }
        if (requestKey.getParamNameAndTypes() == null) {
            requestKey.setParamNameAndTypes(Collections.emptySet());
        }
        return requestKey;
    }

    @FunctionalInterface
    interface ParamAccessor<V> {

        Object access(Map<String, V> params, String name);
    }

    @FunctionalInterface
    interface ParamNameToMissingExceptionConverter {

        MissingRequiredParamException convert(String paramName);
    }

}
