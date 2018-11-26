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

import com.linecorp.clova.extension.boot.exception.InvalidApplicationParameterException;
import com.linecorp.clova.extension.boot.exception.MissingRequiredParamException;
import com.linecorp.clova.extension.boot.exception.UnsupportedHandlerArgumentException;
import com.linecorp.clova.extension.boot.message.request.CEKRequestMessage;

/**
 * Strategy interface for resolving {@link com.linecorp.clova.extension.boot.handler.annnotation.CEKRequestHandler
 * handler} method parameters into argument values.
 */
public interface CEKRequestHandlerArgumentResolver {

    /**
     * Whether the given {@linkplain MethodParameter method parameter} is supported by this resolver.
     *
     * @param methodParam the method parameter to check
     * @return {@code true} if this resolver supports the supplied parameter
     * @throws UnsupportedHandlerArgumentException if this resolver should support it, but the method parameter
     *                                             is contradictory
     */
    boolean supports(MethodParameter methodParam);

    /**
     * Resolves a method parameter into an argument value from a given CEK Request message.
     *
     * @param methodParam    the method parameter to resolve
     * @param requestMessage the CEK request message
     * @return the resolved argument value, or {@code null}
     * @throws InvalidApplicationParameterException in case of conversion error of given value to the method
     *                                              parameter type
     * @throws MissingRequiredParamException        in case of missing required method parameter
     */
    Object resolve(MethodParameter methodParam, CEKRequestMessage requestMessage);

}
