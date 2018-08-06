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

import com.linecorp.clova.extension.boot.handler.CEKHandlerMethod;
import com.linecorp.clova.extension.boot.message.request.CEKRequest;
import com.linecorp.clova.extension.boot.message.request.RequestType;
import com.linecorp.clova.extension.boot.util.LogLevel;

/**
 * An exception for when no applicable {@link CEKHandlerMethod} is found.
 */
@LogLevel(ERROR)
public class RequestHandlerNotFoundException extends InvalidApplicationParameterException {

    private static final long serialVersionUID = 1L;

    private final RequestType requestType;
    private final String name;

    /**
     * Constructs an exception with the {@link RequestType request type} and the {@link CEKRequest#getName() request name}.
     *
     * @param requestType {@link RequestType}
     * @param name        {@link CEKRequest#getName()}
     */
    public RequestHandlerNotFoundException(RequestType requestType, String name) {
        this.requestType = requestType;
        this.name = name;
    }

    @Override
    public String getMessage() {
        return "Handler was not found. [requestType:" + this.requestType + ", name: " + this.name + "]";
    }
}
