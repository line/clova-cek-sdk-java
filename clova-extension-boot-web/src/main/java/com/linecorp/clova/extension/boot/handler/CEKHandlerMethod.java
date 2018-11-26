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

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.core.MethodParameter;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import com.linecorp.clova.extension.boot.handler.annnotation.CEKRequestMapping;
import com.linecorp.clova.extension.boot.handler.annnotation.SlotValue;
import com.linecorp.clova.extension.boot.handler.condition.CEKHandleConditionMatcher;
import com.linecorp.clova.extension.boot.handler.resolver.CEKRequestHandlerArgumentResolver;
import com.linecorp.clova.extension.boot.message.request.CEKRequestMessage;
import com.linecorp.clova.extension.boot.message.request.RequestType;
import com.linecorp.clova.extension.boot.util.StringUtils;

import lombok.Builder;
import lombok.Data;

/**
 * A class to store the Handler information extracted from the {@link CEKRequestMapping @CEKRequestMapping}
 * annotation.
 * <p>
 * This class is able to be sorted by the execution priority. The priority order is decided by the handler class
 * first, and then is decided by the handler methods. The priority is determined as follows.
 * <ol>
 * <li>More {@link #handlerConditionMatchers} size</li>
 * <li>More {@link #methodConditionMatchers} size</li>
 * </ol>
 * If multiple identical priority methods are found, {@link com.linecorp.clova.extension.boot.exception.TooManyMatchedRequestHandlersException
 * TooManyMatchedRequestHandlersException} is thrown by {@link CEKRequestHandlerDispatcher}
 *
 * @see CEKRequestHandlerDispatcher#extractHandlerMethod(javax.servlet.http.HttpServletRequest,
 * com.linecorp.clova.extension.boot.message.request.CEKRequestMessage, com.linecorp.clova.extension.boot.message.context.SystemContext)
 */
@Data
public class CEKHandlerMethod implements Comparable<CEKHandlerMethod> {
    private final RequestType requestType;

    private final Object bean;
    private final Method method;
    private final String name;

    private final List<MethodParameter> methodParams;
    private final List<CEKRequestHandlerArgumentResolver> argumentResolvers;

    private final Set<CEKHandleConditionMatcher> handlerConditionMatchers;
    private final Set<CEKHandleConditionMatcher> methodConditionMatchers;

    private final CEKHandleConditionMatcher compositeMatcher;

    @Builder
    public CEKHandlerMethod(RequestType requestType,
                            Object bean,
                            Method method,
                            String name,
                            List<MethodParameter> methodParams,
                            List<CEKRequestHandlerArgumentResolver> argumentResolvers,
                            Set<CEKHandleConditionMatcher> handlerConditionMatchers,
                            Set<CEKHandleConditionMatcher> methodConditionMatchers) {
        this.requestType = requestType;
        this.bean = bean;
        this.method = method;
        this.name = name;
        this.methodParams = methodParams;
        this.argumentResolvers = argumentResolvers;
        this.handlerConditionMatchers = Optional.ofNullable(handlerConditionMatchers).orElseGet(
                Collections::emptySet);
        this.methodConditionMatchers = Optional.ofNullable(methodConditionMatchers).orElseGet(
                Collections::emptySet);

        this.compositeMatcher = (request, requestMessage, system) -> {
            if (!this.handlerConditionMatchers.isEmpty()
                && !this.handlerConditionMatchers.stream().allMatch(
                    matcher -> matcher.match(request, requestMessage, system))) {
                return false;
            }
            if (!this.methodConditionMatchers.isEmpty()
                && !this.methodConditionMatchers.stream().allMatch(
                    matcher -> matcher.match(request, requestMessage, system))) {
                return false;
            }
            return true;
        };
    }

    public CEKRequestKey createKey() {
        CEKRequestKey requestKey = new CEKRequestKey();
        requestKey.setKey(name);
        requestKey.setParamNameAndTypes(paramNameAndTypes());
        return requestKey;
    }

    public Object[] resolveArguments(CEKRequestMessage requestMessage) {
        return IntStream.range(0, method.getParameterCount())
                        .mapToObj(n -> {
                            MethodParameter methodParam = methodParams.get(n);
                            CEKRequestHandlerArgumentResolver argumentResolver = argumentResolvers.get(n);
                            return argumentResolver.resolve(methodParam, requestMessage);
                        })
                        .toArray();
    }

    public Object invoke(Object[] args) {
        return ReflectionUtils.invokeMethod(method, bean, args);
    }

    private Set<String> paramNameAndTypes() {
        return methodParams.stream()
                           .map(methodParam -> {
                               if (containsType(methodParam, OffsetDateTime.class, ZonedDateTime.class)) {
                                   return getSlotValueName(methodParam) + "@datetime";
                               }
                               if (containsType(methodParam, LocalDate.class)) {
                                   return getSlotValueName(methodParam) + "@date";
                               }
                               if (containsType(methodParam, LocalTime.class)) {
                                   return getSlotValueName(methodParam) + "@time";
                               }
                               return null;
                           })
                           .filter(Objects::nonNull)
                           .collect(Collectors.toSet());
    }

    private boolean containsType(MethodParameter methodParam, Class<?>... types) {
        if (types == null || types.length == 0) {
            return false;
        }
        return Arrays.stream(types)
                     .anyMatch(type -> type == methodParam.getParameterType());
    }

    private String getSlotValueName(MethodParameter methodParameter) {
        return Optional.ofNullable(methodParameter.getParameterAnnotation(SlotValue.class))
                       .map(SlotValue::value)
                       .filter(StringUtils::isNotBlank)
                       .orElseGet(methodParameter::getParameterName);
    }

    @Override
    public int compareTo(CEKHandlerMethod other) {
        int handlerConditionMatcherCompareResult =
                this.getHandlerConditionMatcherClassCount() - other.getHandlerConditionMatcherClassCount();
        if (handlerConditionMatcherCompareResult != 0) {
            // Handler class with more condition types has a higher priority.
            return -1 * handlerConditionMatcherCompareResult;
        }
        int methodConditionMatcherCompareResult =
                this.getMethodConditionMatcherClassCount() - other.getMethodConditionMatcherClassCount();
        if (methodConditionMatcherCompareResult != 0) {
            // Handler method with more condition types has a higher priority.
            return -1 * methodConditionMatcherCompareResult;
        }
        return 0;
    }

    @Override
    public String toString() {
        String conditionMatchersString = conditionMatchersToString();
        if (conditionMatchersString.isEmpty()) {
            return String.format("%s:\"%s\" %s#%s(%s)",
                                 this.requestType.name().toLowerCase(),
                                 this.name,
                                 method.getDeclaringClass().getSimpleName(),
                                 method.getName(), methodParamsToString());
        } else {
            return String.format("%s:\"%s\" %s#%s(%s) conditions:[%s]",
                                 this.requestType.name().toLowerCase(),
                                 this.name,
                                 method.getDeclaringClass().getSimpleName(),
                                 method.getName(), methodParamsToString(),
                                 conditionMatchersString);
        }
    }

    private String conditionMatchersToString() {
        List<String> strings = new ArrayList<>();
        if (!this.handlerConditionMatchers.isEmpty()) {
            strings.add("handler:" + String.valueOf(this.handlerConditionMatchers));
        }
        if (!this.methodConditionMatchers.isEmpty()) {
            strings.add("method:" + String.valueOf(this.methodConditionMatchers));
        }
        return strings.stream().collect(Collectors.joining(" "));
    }

    private String methodParamsToString() {
        return methodParams.stream()
                           .map(methodParam -> methodParam.getParameterType().getSimpleName() + " "
                                               + methodParam.getParameterName())
                           .collect(Collectors.joining(", "));
    }

    private int getHandlerConditionMatcherClassCount() {
        return getConditionMatcherClassCount(this.handlerConditionMatchers);
    }

    private int getMethodConditionMatcherClassCount() {
        return getConditionMatcherClassCount(this.methodConditionMatchers);
    }

    private static int getConditionMatcherClassCount(Collection<CEKHandleConditionMatcher> matchers) {
        return (int) matchers.stream()
                             .map(CEKHandlerMethod::getConditionMatcherClassName)
                             .distinct()
                             .count();
    }

    private static String getConditionMatcherClassName(CEKHandleConditionMatcher matcher) {
        Class<?> matcherType = ClassUtils.getUserClass(AopProxyUtils.ultimateTargetClass(matcher));
        return matcherType.getName();
    }

}
