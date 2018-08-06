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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.core.MethodParameter;
import org.springframework.util.ReflectionUtils;

import com.linecorp.clova.extension.boot.handler.annnotation.CEKRequestMapping;
import com.linecorp.clova.extension.boot.handler.annnotation.SlotValue;
import com.linecorp.clova.extension.boot.handler.condition.CEKHandleConditionMatcher;
import com.linecorp.clova.extension.boot.message.request.RequestType;
import com.linecorp.clova.extension.boot.util.StringUtils;

import lombok.Builder;
import lombok.Data;

/**
 * A class to store the Handler information extracted from the {@link CEKRequestMapping @CEKRequestMapping} annotation.
 */
@Data
@Builder
public class CEKHandlerMethod {
    private final RequestType requestType;

    private final Object bean;
    private final Method method;
    private final String name;

    private final List<MethodParameter> methodParams;

    private final Set<CEKHandleConditionMatcher> conditionMatchers;

    public CEKRequestKey createKey() {
        CEKRequestKey requestKey = new CEKRequestKey();
        requestKey.setKey(name);
        requestKey.setParamNameAndTypes(paramNameAndTypes());
        return requestKey;
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
    public String toString() {
        if (this.conditionMatchers == null || this.conditionMatchers.isEmpty()) {
            return String.format("%s:\"%s\" %s#%s(%s)",
                                 this.requestType.name().toLowerCase(),
                                 this.name,
                                 method.getDeclaringClass().getSimpleName(),
                                 method.getName(), methodParamsToString());
        }
        return String.format("%s:\"%s\" %s#%s(%s) conditions:%s",
                             this.requestType.name().toLowerCase(),
                             this.name,
                             method.getDeclaringClass().getSimpleName(),
                             method.getName(), methodParamsToString(), this.conditionMatchers.toString());
    }

    private String methodParamsToString() {
        return methodParams.stream()
                           .map(methodParam -> methodParam.getParameterType().getSimpleName() + " "
                                               + methodParam.getParameterName())
                           .collect(Collectors.joining(", "));
    }
}
