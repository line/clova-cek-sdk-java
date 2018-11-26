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

import javax.servlet.http.HttpServletResponse;

import org.springframework.core.MethodParameter;
import org.springframework.util.Assert;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.linecorp.clova.extension.boot.message.request.CEKRequestMessage;

/**
 * {@link CEKRequestHandlerArgumentResolver} for {@link HttpServletResponse}.
 */
public class HttpServletResponseArgumentResolver implements CEKRequestHandlerArgumentResolver {

    @Override
    public boolean supports(MethodParameter methodParam) {
        return HttpServletResponse.class.isAssignableFrom(methodParam.getParameterType());
    }

    @Override
    public Object resolve(MethodParameter methodParam, CEKRequestMessage requestMessage) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        Assert.isInstanceOf(ServletRequestAttributes.class, requestAttributes);

        return ((ServletRequestAttributes) requestAttributes).getResponse();
    }

}
