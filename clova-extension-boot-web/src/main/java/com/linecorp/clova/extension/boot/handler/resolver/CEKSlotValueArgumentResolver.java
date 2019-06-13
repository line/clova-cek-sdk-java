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

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Optional;

import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.linecorp.clova.extension.boot.exception.InvalidApplicationParameterException;
import com.linecorp.clova.extension.boot.exception.InvalidSlotException;
import com.linecorp.clova.extension.boot.exception.MissingSlotException;
import com.linecorp.clova.extension.boot.exception.UnsupportedHandlerArgumentException;
import com.linecorp.clova.extension.boot.handler.annnotation.CEKRequestMapping;
import com.linecorp.clova.extension.boot.handler.annnotation.SlotValue;
import com.linecorp.clova.extension.boot.message.request.CEKRequestMessage;
import com.linecorp.clova.extension.boot.message.request.DefaultSlotValueUnit;
import com.linecorp.clova.extension.boot.message.request.IntentRequest;
import com.linecorp.clova.extension.boot.message.request.RequestType;
import com.linecorp.clova.extension.boot.message.request.Slot;
import com.linecorp.clova.extension.boot.message.request.SlotValueType;
import com.linecorp.clova.extension.boot.message.request.SlotValueUnit;
import com.linecorp.clova.extension.boot.util.StringUtils;

/**
 * {@link CEKRequestHandlerArgumentResolver} for extracting a slot value.
 */
public class CEKSlotValueArgumentResolver extends CEKRequestHandlerArgumentResolverSupport {

    private static final String PARAMS_NAME = "slots";

    public CEKSlotValueArgumentResolver(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public boolean supports(MethodParameter methodParam) {
        Method method = methodParam.getMethod();
        Assert.notNull(method, "This MethodParameter doesn't have Method. method parameter:" + methodParam);
        if (methodParam.hasParameterAnnotation(SlotValue.class)
            || canConvert(Slot.class, methodParam)
            || canConvert(SlotValueUnit.class, methodParam)) {
            CEKRequestMapping requestMapping = AnnotationUtils.getAnnotation(method, CEKRequestMapping.class);
            Assert.notNull(requestMapping, "This method is not handler. method:" + method);
            if (requestMapping.type() != RequestType.INTENT) {
                throw new UnsupportedHandlerArgumentException(methodParam,
                                                              "Only Intent Handler can handle slot value.");
            }
            return true;
        }
        return false;
    }

    @Override
    public Object resolve(MethodParameter methodParam, CEKRequestMessage requestMessage) {
        IntentRequest intentRequest = (IntentRequest) requestMessage.getRequest();

        return extractAndConvertMethodParam(PARAMS_NAME, intentRequest.getIntent().getSlots(),
                                            (slots, name) -> Optional.ofNullable(slots)
                                                                     .map(s -> s.get(name))
                                                                     .orElse(null),
                                            methodParam, MissingSlotException::new);
    }

    @Override
    @SuppressWarnings({ "rawTypes", "unchecked" })
    protected Object convertValue(Object object, MethodParameter methodParam) {
        Slot slot = (Slot) object;
        SlotValueType slotValueType = slot.getValueType();

        try {
            if (slotValueType == null) {
                if (canConvert(Slot.class, methodParam)) {
                    return doConvertValue(slot, methodParam);
                }
                Type defaultSlotValueUnitType = asDefaultSlotValueUnitIfPossible(methodParam);
                if (defaultSlotValueUnitType != null) {
                    return doConvertValue(slot, defaultSlotValueUnitType);
                }
                if (canConvert(SlotValueUnit.class, methodParam)) {
                    return doConvertValue(slot, methodParam);
                }
                return doConvertValue(slot.getValue(), methodParam);
            }

            String slotValue = (String) slot.getValue();
            ResolvableType methodParamTypeWithoutOptional = slotValueTypeWithoutOptionalFrom(methodParam);

            if (methodParamTypeWithoutOptional.resolve() == String.class) {
                if (canConvert(Slot.class, methodParam)) {
                    return doConvertValue(slot, methodParam);
                }
                return doConvertValue(slotValue, methodParam);
            }

            Object convertedSlotValue = slotValueType.parse(slotValue);
            ResolvableType methodParamType = ResolvableType.forMethodParameter(methodParam);
            if (methodParamType.resolve() == Optional.class) {
                if (methodParamType.getGeneric().resolve() == Slot.class) {
                    if (methodParamType.getGeneric().getGeneric().resolve(Object.class)
                                       .isAssignableFrom(convertedSlotValue.getClass())) {
                        return Optional.of(slot.withNewValue(convertedSlotValue));
                    }
                } else if (methodParamType.getGeneric().resolve(Object.class)
                                          .isAssignableFrom(convertedSlotValue.getClass())) {
                    return Optional.of(convertedSlotValue);
                }
            } else if (methodParamType.resolve() == Slot.class) {
                if (methodParamType.getGeneric().resolve(Object.class)
                                   .isAssignableFrom(convertedSlotValue.getClass())) {
                    return slot.withNewValue(convertedSlotValue);
                }
            } else if (methodParamType.resolve(Object.class)
                                      .isAssignableFrom(convertedSlotValue.getClass())) {
                return convertedSlotValue;
            }
            throw new InvalidApplicationParameterException(
                    "Failed to mapping. [" + object + " -> "
                    + StringUtils.methodParamToString(methodParam) + "]");

        } catch (Exception e) {
            throw new InvalidSlotException(slot, methodParam, e);
        }
    }

    private Object doConvertValue(Object value, MethodParameter methodParam) {
        return doConvertValue(value, methodParam.getGenericParameterType());
    }

    private Object doConvertValue(Object value, Type type) {
        return getObjectMapper().convertValue(value, getObjectMapper().getTypeFactory().constructType(type));
    }

    @Nullable
    private static Type asDefaultSlotValueUnitIfPossible(MethodParameter methodParam) {
        if (methodParam.getParameterType() == Optional.class) {
            ResolvableType optionalType = ResolvableType.forMethodParameter(methodParam);
            ResolvableType slotValueUnitType = optionalType.getGeneric();
            ResolvableType defaultSlotValueUnitType = slotValueUnitTypeToDefaultType(slotValueUnitType);
            if (defaultSlotValueUnitType != null) {
                return ResolvableType.forClassWithGenerics(Optional.class, defaultSlotValueUnitType).getType();
            }
            return null;
        }
        ResolvableType slotValueUnitType = ResolvableType.forMethodParameter(methodParam);
        ResolvableType defaultSlotValueUnitType = slotValueUnitTypeToDefaultType(slotValueUnitType);
        if (defaultSlotValueUnitType != null) {
            return defaultSlotValueUnitType.getType();
        }
        return null;
    }

    @Nullable
    private static ResolvableType slotValueUnitTypeToDefaultType(ResolvableType slotValueUnitType) {
        Class<?> slotValueUnitClass = slotValueUnitType.resolve();
        if (slotValueUnitClass == SlotValueUnit.class || slotValueUnitClass == DefaultSlotValueUnit.class) {
            ResolvableType valueType = slotValueUnitType.getGeneric(0);
            ResolvableType unitType = slotValueUnitType.getGeneric(1);
            return ResolvableType.forClassWithGenerics(DefaultSlotValueUnit.class,
                                                       valueType, unitType);
        }
        return null;
    }

    private static ResolvableType slotValueTypeWithoutOptionalFrom(MethodParameter methodParam) {
        ResolvableType slotValueType = ResolvableType.forMethodParameter(methodParam);
        // Optional<Slot<T>> -> Slot<T>
        if (Optional.class.isAssignableFrom(slotValueType.resolve(Object.class))) {
            slotValueType = slotValueType.getGeneric();
        }
        // Slot<T> -> T
        if (Slot.class.isAssignableFrom(slotValueType.resolve(Object.class))) {
            slotValueType = slotValueType.getGeneric();
        }
        return slotValueType;
    }
}
