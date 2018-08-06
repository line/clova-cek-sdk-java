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

import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.SmartValidator;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import com.linecorp.clova.extension.boot.message.response.CEKResponse;
import com.linecorp.clova.extension.boot.message.response.CEKResponseMessage;
import com.linecorp.clova.extension.boot.util.LogUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link ResponseBodyAdvice} that validates the response body.
 * <p>
 * The validation errors are logged out with WARN level.
 * This advice never throws validation error.
 */
@RestControllerAdvice(annotations = { RestController.class, RestControllerAdvice.class })
@Order(Ordered.LOWEST_PRECEDENCE - 1)
@RequiredArgsConstructor
@Slf4j
public class CEKResponseMessageValidationAdvice extends CEKResponseMessageBodyAbstractAdvice {

    private final SmartValidator validator;

    @Override
    public boolean supports(MethodParameter returnType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return log.isDebugEnabled() && super.supports(returnType, converterType);
    }

    @Nullable
    @Override
    public Object beforeBodyWrite(@Nullable Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {
        if (body == null) {
            return new CEKResponseMessage(CEKResponse.empty());
        }

        try {
            BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(body, "responseMessage");
            validator.validate(body, bindingResult);

            if (bindingResult.hasErrors()) {
                log.debug("Invalid CEK response. Please check the following errors.\n"
                          + bindingResult);
            }
        } catch (Throwable t) {
            LogUtils.logging(log, t);
        }

        return body;
    }
}
