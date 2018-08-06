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

import java.util.Collection;

import com.linecorp.clova.extension.boot.handler.CEKHandlerMethod;
import com.linecorp.clova.extension.boot.message.request.CEKRequest;
import com.linecorp.clova.extension.boot.message.request.RequestType;
import com.linecorp.clova.extension.boot.util.LogLevel;

/**
 * An exception for when more than two applicable {@link CEKHandlerMethod}s are found.
 */
@LogLevel(ERROR)
public class TooManyMatchedRequestHandlersException extends InvalidApplicationParameterException {

    private static final long serialVersionUID = 1L;

    private final RequestType requestType;
    private final String name;
    private final Collection<CEKHandlerMethod> handlerMethods;

    /**
     * Constructs an exception with the {@link RequestType request type} and the {@link CEKRequest#getName() request name}
     * and the applicable {@link CEKHandlerMethod handler method}s.
     *
     * @param requestType    {@link RequestType}
     * @param name           {@link CEKRequest#getName()}
     * @param handlerMethods applicable {@link CEKHandlerMethod handler method}s
     */
    public TooManyMatchedRequestHandlersException(RequestType requestType, String name,
                                                  Collection<CEKHandlerMethod> handlerMethods) {
        this.requestType = requestType;
        this.name = name;
        this.handlerMethods = handlerMethods;
    }

    @Override
    public String getMessage() {
        return "Handler was not found. [requestType:" + this.requestType + " name: " + this.name
               + " handlerMethods:" + this.handlerMethods + "]";
    }

}
