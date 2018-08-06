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

package com.linecorp.clova.extension.boot.exception;

import static com.linecorp.clova.extension.boot.util.InformationLevel.ERROR;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.linecorp.clova.extension.boot.handler.CEKHandlerMethod;
import com.linecorp.clova.extension.boot.handler.interceptor.CEKHandlerInterceptor;
import com.linecorp.clova.extension.boot.util.LogLevel;

/**
 * An exception for errors occurred while processing {@link CEKHandlerInterceptor}.
 */
@LogLevel(ERROR)
public class CEKHandlerInterceptException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final CEKHandlerMethod handlerMethod;
    private final List<?> args;

    /**
     * Constructs an exception with the specified detail message.
     *
     * @param handlerMethod {@link CEKHandlerMethod}
     * @param args          arguments of handler method.
     * @param message       detail message.
     * @param e             original occurred exception while processing {@link CEKHandlerInterceptor}.
     */
    public CEKHandlerInterceptException(CEKHandlerMethod handlerMethod, Object[] args, String message,
                                        Exception e) {
        super(message, e);
        this.handlerMethod = handlerMethod;
        this.args = args != null ? Arrays.asList(args) : Collections.emptyList();
    }

    /**
     * Constructs an exception.
     *
     * @param handlerMethod {@link CEKHandlerMethod}
     * @param args          arguments of handler method.
     * @param e             original occurred exception while processing {@link CEKHandlerInterceptor}.
     */
    public CEKHandlerInterceptException(CEKHandlerMethod handlerMethod, Object[] args, Exception e) {
        super(e);
        this.handlerMethod = handlerMethod;
        this.args = args != null ? Arrays.asList(args) : Collections.emptyList();
    }

    @Override
    public String getMessage() {
        return "An error occurred while processing CEKHandlerInterceptor. " +
               "[handlerMethod: " + this.handlerMethod + ", args:" + this.args + "]";
    }

}
