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

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.core.MethodParameter;

import com.linecorp.clova.extension.boot.util.LogLevel;
import com.linecorp.clova.extension.boot.util.StringUtils;

/**
 * An exception for unsupported handler arguments.
 * <p>
 * This is thrown when the request parameter type is different from expected one (the handler argument type).
 */
@LogLevel(ERROR)
public class UnsupportedHandlerArgumentException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final MethodParameter methodParam;

    /**
     * Constructs an exception with the expected {@link MethodParameter method parameter}.
     *
     * @param methodParam the expected {@link MethodParameter method parameter}
     */
    public UnsupportedHandlerArgumentException(MethodParameter methodParam) {
        super((String) null);
        this.methodParam = methodParam;
    }

    /**
     * Constructs an exception with the expected {@link MethodParameter method parameter} and the specified
     * detail message.
     *
     * @param methodParam the expected {@link MethodParameter method parameter}
     * @param message     detail message
     */
    public UnsupportedHandlerArgumentException(MethodParameter methodParam, String message) {
        super(message);
        this.methodParam = methodParam;
    }

    @Override
    public String getMessage() {
        return Stream.<String>builder()
                .add(super.getMessage())
                .add("Found unsupported handler's argument. [" +
                     StringUtils.methodParamToString(this.methodParam) + "]")
                .build()
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining("\n"));
    }

}
