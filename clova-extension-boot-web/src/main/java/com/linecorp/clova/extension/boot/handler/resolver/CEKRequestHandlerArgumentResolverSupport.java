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

package com.linecorp.clova.extension.boot.handler.resolver;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.linecorp.clova.extension.boot.exception.InvalidApplicationParameterException;
import com.linecorp.clova.extension.boot.exception.MissingRequiredParamException;
import com.linecorp.clova.extension.boot.handler.annnotation.CEKRequestParam;
import com.linecorp.clova.extension.boot.util.RequestUtils;
import com.linecorp.clova.extension.boot.util.StringUtils;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Support class for {@link CEKRequestHandlerArgumentResolver} that requires complex conversion of arguments.
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class CEKRequestHandlerArgumentResolverSupport implements CEKRequestHandlerArgumentResolver {

    private static final String CAMEL_CONVERTED_MAP_REQUEST_ATTR_KEY =
            CEKRequestHandlerArgumentResolverSupport.class + ".CAMEL_CONVERTED_MAP";

    private static final Map<Predicate<String>, UnaryOperator<String>> CAMEL_CONVERTERS_BY_CONDITION;

    static {
        CAMEL_CONVERTERS_BY_CONDITION = new HashMap<>();
        CAMEL_CONVERTERS_BY_CONDITION.put(StringUtils::isPascalCase, StringUtils::pascalToCamel);
        CAMEL_CONVERTERS_BY_CONDITION.put(StringUtils::isLowerSnakeCase, StringUtils::lowerSnakeToCamel);
        CAMEL_CONVERTERS_BY_CONDITION.put(StringUtils::isUpperSnakeCase, StringUtils::upperSnakeToCamel);
        CAMEL_CONVERTERS_BY_CONDITION.put(StringUtils::isLowerKebabCase, StringUtils::lowerKebabToCamel);
        CAMEL_CONVERTERS_BY_CONDITION.put(StringUtils::isUpperKebabCase, StringUtils::upperKebabToCamel);
    }

    @Getter(AccessLevel.PROTECTED)
    private final ObjectMapper objectMapper;

    /**
     * Whether the specified type can be assigned to this method parameter type.
     * <p>
     * When the method parameter type is {@link Optional}, returns whether the specified type can be assigned to
     * the generic type.
     *
     * @param allowedType to check whether this type can be assigned to the method parameter type
     * @param methodParam to check whether the specified type can be assigned to this method parameter type
     * @return {@code true} if the specified type can be assigned to this method parameter type
     */
    protected static boolean canConvert(Type allowedType, MethodParameter methodParam) {
        ResolvableType allowedResolvableType = ResolvableType.forType(allowedType);
        ResolvableType paramResolvableType = ResolvableType.forMethodParameter(methodParam);

        if (Optional.class.isAssignableFrom(paramResolvableType.resolve())) {
            return allowedResolvableType.isAssignableFrom(paramResolvableType.getGeneric());
        }

        return allowedResolvableType.isAssignableFrom(paramResolvableType);
    }

    /**
     * Alternative to {@link #extractAndConvertMethodParam(ParamsWithName, ParamAccessor, MethodParameter,
     * ParamNameToMissingExceptionConverter)}.
     * <p>
     * Generates {@link ParamsWithName} from {@code paramsName} and {@code params}, and passes this to the
     * alternative method. To pass {@link ParamAccessor} is simple {@link Map#get(Object)}.
     *
     * @param paramsName       the name of {@code params}; using this as a key, controls the process of
     *                         generating the case of variable case only once
     * @param params           may contain a value to extract
     * @param methodParam      converts the value to this method parameter type
     * @param throwerIfMissing if the value to extract isn't in the {@code params}, generates {@link
     *                         MissingRequiredParamException} and throws it; however, it does not apply if the
     *                         method parameter does not necessarily need a value
     * @param <V>              {@code params} values type
     * @return extracted and converted value
     */
    protected <V> Object extractAndConvertMethodParam(@NonNull String paramsName,
                                                      Map<String, V> params,
                                                      @NonNull MethodParameter methodParam,
                                                      @NonNull ParamNameToMissingExceptionConverter
                                                              throwerIfMissing) {
        return extractAndConvertMethodParam(new ParamsWithName<>(paramsName, params), Map::get, methodParam,
                                            throwerIfMissing);
    }

    /**
     * Alternative to {@link #extractAndConvertMethodParam(ParamsWithName, ParamAccessor, MethodParameter,
     * ParamNameToMissingExceptionConverter)}.
     * <p>
     * To pass {@link ParamAccessor} is simple {@link Map#get(Object)}.
     *
     * @param paramsWithName   parameters and its name; the parameters may contain a value to extract; the name
     *                         is used as a key to control the process of generating the case of variable case
     *                         only once
     * @param methodParam      converts the value to this method parameter type
     * @param throwerIfMissing if the value to extract isn't in the {@code params}, generates {@link
     *                         MissingRequiredParamException} and throws it; however, it does not apply if the
     *                         method parameter does not necessarily need a value
     * @param <V>              the specified parameter values type
     * @return extracted and converted value
     */
    protected <V> Object extractAndConvertMethodParam(@NonNull ParamsWithName<V> paramsWithName,
                                                      @NonNull MethodParameter methodParam,
                                                      @NonNull ParamNameToMissingExceptionConverter
                                                              throwerIfMissing) {
        return extractAndConvertMethodParam(paramsWithName, Map::get, methodParam, throwerIfMissing);
    }

    /**
     * Alternative to {@link #extractAndConvertMethodParam(ParamsWithName, ParamAccessor, MethodParameter,
     * ParamNameToMissingExceptionConverter)}.
     * <p>
     * Generates {@link ParamsWithName} from {@code paramsName} and {@code params} and passes it to the
     * alternative method.
     *
     * @param paramsName       the name of {@code params}; using this as a key, controls the process of
     *                         generating the case of variable case only once
     * @param params           may contain a value to extract
     * @param paramAccessor    a function that accesses the value contained in the specified parameters
     * @param methodParam      converts the value to this method parameter type
     * @param throwerIfMissing if the value to extract isn't in the {@code params}, generates {@link
     *                         MissingRequiredParamException} and throws it; however, it does not apply if the
     *                         method parameter does not necessarily need a value
     * @param <V>              {@code params} values type
     * @return extracted and converted value
     */
    protected <V> Object extractAndConvertMethodParam(@NonNull String paramsName,
                                                      Map<String, V> params,
                                                      @NonNull ParamAccessor<V> paramAccessor,
                                                      @NonNull MethodParameter methodParam,
                                                      @NonNull ParamNameToMissingExceptionConverter
                                                              throwerIfMissing) {
        return extractAndConvertMethodParam(new ParamsWithName<>(paramsName, params), paramAccessor,
                                            methodParam,
                                            throwerIfMissing);
    }

    /**
     * Extracts a value from specified parameters and convert to assign the method parameter type.
     *
     * @param paramsWithName   parameters and its name; the parameters may contain a value to extract; the name
     *                         is used as a key to control the process of generating the case of variable case
     *                         only once
     * @param paramAccessor    a function that accesses the value contained in the specified parameters
     * @param methodParam      converts the value to this method parameter type
     * @param throwerIfMissing if the value to extract isn't in the {@code params}, generates {@link
     *                         MissingRequiredParamException} and throws it; however, it does not apply if the
     *                         method parameter does not necessarily need a value
     * @param <V>              the specified parameter values type
     * @return extracted and converted value
     */
    @SuppressWarnings("unchecked")
    protected <V> Object extractAndConvertMethodParam(@NonNull ParamsWithName<V> paramsWithName,
                                                      @NonNull ParamAccessor<V> paramAccessor,
                                                      @NonNull MethodParameter methodParam,
                                                      @NonNull ParamNameToMissingExceptionConverter
                                                              throwerIfMissing) {
        Map<String, V> camelSupported = supportAlsoCamel(paramsWithName);

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

        if (CollectionUtils.isEmpty(paramsWithName.getParams()) || paramValue == null) {
            if (hasRequiredOrValueIsTrue(annotation)) {
                throw throwerIfMissing.convert(name);
            } else {
                return null;
            }
        }

        return convertValue(paramValue, methodParam);
    }

    private static boolean hasRequiredOrValueIsTrue(Annotation annotation) {
        if (annotation == null) {
            return true;
        }
        Boolean required = (Boolean) AnnotationUtils.getValue(annotation, "required");
        if (required == null) {
            return true;
        }
        return required;
    }

    /**
     * Converts the object to the specified method parameter type using {@link ObjectMapper}.
     *
     * @param object      a value to convert
     * @param methodParam to convert the {@code object} to this method parameter type
     * @return converted value
     */
    protected Object convertValue(Object object, MethodParameter methodParam) {
        try {
            return objectMapper.convertValue(object, objectMapper.getTypeFactory().constructType(
                    methodParam.getGenericParameterType()));
        } catch (Exception e) {
            throw new InvalidApplicationParameterException(
                    "Failed to mapping. [" + object + " -> " + StringUtils.methodParamToString(methodParam)
                    + "]", e);
        }
    }

    @SuppressWarnings("unchecked")
    private <V> Map<String, V> supportAlsoCamel(ParamsWithName<V> paramsWithName) {
        Map<String, V> params = paramsWithName.getParams();
        if (params == null || params.isEmpty()) {
            return Collections.emptyMap();
        }

        HttpServletRequest request = RequestUtils.getCurrentHttpRequest();
        Map<String, Map<String, ?>> camelConvertedMap =
                (Map<String, Map<String, ?>>) request.getAttribute(CAMEL_CONVERTED_MAP_REQUEST_ATTR_KEY);
        if (camelConvertedMap == null) {
            camelConvertedMap = new HashMap<>();
            request.setAttribute(CAMEL_CONVERTED_MAP_REQUEST_ATTR_KEY, camelConvertedMap);
        }

        return (Map<String, V>) camelConvertedMap.computeIfAbsent(
                paramsWithName.getParamsName(),
                name -> {
                    Map<String, V> newParams = new HashMap<>(params);
                    params.forEach((key, value) -> {
                        CAMEL_CONVERTERS_BY_CONDITION
                                .entrySet().stream()
                                .filter(converterWithCondition -> converterWithCondition.getKey()
                                                                                        .test(key))
                                .findFirst()
                                .map(converterWithCondition -> converterWithCondition.getValue()
                                                                                     .apply(key))
                                .ifPresent(camelKey -> newParams.put(camelKey, value));
                    });
                    return newParams;
                });
    }

    /**
     * A functional interface for access the value to extract.
     *
     * @param <V> the value type of the {@link Map} that contains the value to extract
     */
    @FunctionalInterface
    protected interface ParamAccessor<V> {

        /**
         * Accesses the value to extract from the specified params using given name.
         *
         * @param params this may contain the value to extract
         * @param name   something like a key or hint to access the value to extract
         * @return the value to extract; however, this should not necessarily match the type of the {@code
         * params} value
         */
        Object access(Map<String, V> params, String name);
    }

    /**
     * A functional interface for {@link MissingRequiredParamException} generation.
     */
    @FunctionalInterface
    protected interface ParamNameToMissingExceptionConverter {

        /**
         * @param paramName the name corresponding to the value to extract; using this, calls {@link
         *                  MissingRequiredParamException#MissingRequiredParamException(String)}
         * @return {@link MissingRequiredParamException} instance.
         */
        MissingRequiredParamException convert(String paramName);
    }

    /**
     * A class that holds parameters and its name.
     * <p>
     * The parameters may contain a value to extract. The name is used as a key, control the process of
     * generating the case of variable case only once.
     *
     * @param <V> type of the parameter values
     */
    @Data
    protected static class ParamsWithName<V> {

        private final String paramsName;
        private final Map<String, V> params;

    }

}
