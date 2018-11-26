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

import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.linecorp.clova.extension.boot.exception.UnsupportedHandlerArgumentException;
import com.linecorp.clova.extension.boot.handler.annnotation.CEKRequestMapping;
import com.linecorp.clova.extension.boot.message.payload.Payload;
import com.linecorp.clova.extension.boot.message.request.CEKRequestMessage;
import com.linecorp.clova.extension.boot.message.request.EventRequest;
import com.linecorp.clova.extension.boot.message.request.RequestType;

/**
 * {@link CEKRequestHandlerArgumentResolver} for extracting CEK Event {@link Payload}.
 */
public class CEKEventPayloadArgumentResolver extends CEKRequestHandlerArgumentResolverSupport {

    public CEKEventPayloadArgumentResolver(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public boolean supports(MethodParameter methodParam) {
        Method method = methodParam.getMethod();
        Assert.notNull(method, "This MethodParameter doesn't have Method. method parameter:" + methodParam);
        if (Payload.class.isAssignableFrom(methodParam.getParameterType())) {
            CEKRequestMapping requestMapping = AnnotationUtils.getAnnotation(method, CEKRequestMapping.class);
            Assert.notNull(requestMapping, "This method is not handler. method:" + method);
            if (requestMapping.type() != RequestType.EVENT) {
                throw new UnsupportedHandlerArgumentException(methodParam,
                                                              "Only Event Handler can handle event payload.");
            }
            return true;
        }
        return false;
    }

    @Override
    public Object resolve(MethodParameter methodParam, CEKRequestMessage requestMessage) {
        EventRequest eventRequest = (EventRequest) requestMessage.getRequest();

        Payload payload = eventRequest.getEvent().getPayload();
        return convertValue(payload, methodParam);
    }

}
