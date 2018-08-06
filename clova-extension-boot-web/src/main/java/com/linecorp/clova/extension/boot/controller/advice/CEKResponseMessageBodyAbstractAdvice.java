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

package com.linecorp.clova.extension.boot.controller.advice;

import java.util.Optional;

import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import com.linecorp.clova.extension.boot.message.response.CEKResponseMessage;

/**
 * Abstract {@link ResponseBodyAdvice} for {@link CEKResponseMessage}.
 */
public abstract class CEKResponseMessageBodyAbstractAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        if (HttpEntity.class.isAssignableFrom(returnType.getParameterType())
            || ResponseEntity.class.isAssignableFrom(returnType.getParameterType())) {
            ResolvableType resolvableType = ResolvableType.forMethodParameter(returnType);

            return Optional.ofNullable(resolvableType.getGeneric())
                           .map(ResolvableType::resolve)
                           .filter(CEKResponseMessage.class::isAssignableFrom)
                           .isPresent();
        }

        return CEKResponseMessage.class.isAssignableFrom(returnType.getParameterType());
    }

}
