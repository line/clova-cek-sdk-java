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

package com.linecorp.clova.extension.boot.handler.interceptor;

import javax.servlet.http.HttpServletRequest;

import com.linecorp.clova.extension.boot.handler.CEKHandlerMethod;
import com.linecorp.clova.extension.boot.message.request.CEKRequestMessage;
import com.linecorp.clova.extension.boot.message.response.CEKResponseMessage;

/**
 * Interceptor for {@code CEKRequestHandler}.
 * <p>
 * Before calling {@code CEKRequestHandler} method, call this {@link #preHandle(HttpServletRequest, CEKRequestMessage, CEKHandlerMethod, Object[]) preHandle}.
 * After calling {@code CEKRequestHandler} method, call this {@link #postHandle(HttpServletRequest, CEKRequestMessage, CEKResponseMessage, CEKHandlerMethod, Object[]) postHandle}.
 * <p>
 * If {@link #shouldNotIntercept(HttpServletRequest, CEKRequestMessage, CEKHandlerMethod, Object[]) shouldNotIntercept(...)} returns {@code true},
 * Both {@code preHandle} and {@code postHandle} are not called.
 * <p>
 * {@code shouldNotIntercept} is called <b>only once</b> before calling {@code preHandle}.
 * <p>
 * If {@code preHandle} is called, {@code postHandle} is <b>ALWAYS</b> called even if the handler threw any throwable.
 */
public interface CEKHandlerInterceptor {

    default boolean shouldNotIntercept(HttpServletRequest request, CEKRequestMessage requestMessage,
                                       CEKHandlerMethod handlerMethod, Object[] args) {
        return false;
    }

    default void preHandle(HttpServletRequest request, CEKRequestMessage requestMessage,
                           CEKHandlerMethod handlerMethod, Object[] args) throws Exception {
        // nop
    }

    default void postHandle(HttpServletRequest request, CEKRequestMessage requestMessage,
                            CEKResponseMessage responseMessage,
                            CEKHandlerMethod handlerMethod, Object[] args) throws Exception {
        // nop
    }

}
