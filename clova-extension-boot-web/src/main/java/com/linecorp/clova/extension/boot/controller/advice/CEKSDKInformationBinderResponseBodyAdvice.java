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

import java.io.Serializable;

import org.springframework.beans.BeanUtils;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import com.linecorp.clova.extension.boot.ClovaExtensionBootVersion;
import com.linecorp.clova.extension.boot.message.response.CEKResponse;
import com.linecorp.clova.extension.boot.message.response.CEKResponseMessage;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link ResponseBodyAdvice} that sets this SDK information in the response body.
 */
@RestControllerAdvice(annotations = { RestController.class, RestControllerAdvice.class })
@Order
@Slf4j
public class CEKSDKInformationBinderResponseBodyAdvice extends CEKResponseMessageBodyAbstractAdvice {

    static final String CEK_SDK_NAME = "clova-cek-sdk-java";

    @Override
    public Object beforeBodyWrite(@Nullable Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {
        CEKResponseMessage responseMessage = body != null
                                             ? (CEKResponseMessage) body
                                             : new CEKResponseMessage(CEKResponse.empty());
        try {
            return new CEKResponseMessageWrapper(responseMessage);
        } catch (Throwable t) {
            // nop
        }
        return body;
    }

    @Getter
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    static class CEKResponseMessageWrapper extends CEKResponseMessage {

        private static final long serialVersionUID = 1L;

        private final Meta meta;

        CEKResponseMessageWrapper(CEKResponseMessage message) {
            super(message.getResponse());
            BeanUtils.copyProperties(message, this);
            this.meta = Meta.builder()
                            .customExtensionSdk(CEK_SDK_NAME)
                            .customExtensionSdkVersion(ClovaExtensionBootVersion.getVersion())
                            .build();
        }

        @Getter
        @EqualsAndHashCode
        @ToString
        @Builder
        static class Meta implements Serializable {

            private static final long serialVersionUID = 1L;

            private final String customExtensionSdk;
            private final String customExtensionSdkVersion;
        }
    }

}
