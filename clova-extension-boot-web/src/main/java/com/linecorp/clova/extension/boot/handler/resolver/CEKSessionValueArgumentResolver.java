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

import org.springframework.core.MethodParameter;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.linecorp.clova.extension.boot.exception.MissingSessionAttributeException;
import com.linecorp.clova.extension.boot.handler.annnotation.SessionValue;
import com.linecorp.clova.extension.boot.message.request.CEKRequestMessage;

/**
 * {@link CEKRequestHandlerArgumentResolver} for extracting a session attribute value.
 */
public class CEKSessionValueArgumentResolver extends CEKRequestHandlerArgumentResolverSupport {

    private static final String PARAMS_NAME = "session_attributes";

    public CEKSessionValueArgumentResolver(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public boolean supports(MethodParameter methodParam) {
        return methodParam.hasParameterAnnotation(SessionValue.class);
    }

    @Override
    public Object resolve(MethodParameter methodParam, CEKRequestMessage requestMessage) {
        return extractAndConvertMethodParam(PARAMS_NAME, requestMessage.getSession().getSessionAttributes(),
                                            methodParam, MissingSessionAttributeException::new);
    }
}
