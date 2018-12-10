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

import com.linecorp.clova.extension.boot.util.LogLevel;

/**
 * An exception for when an invalid parameter is set by the Clova application.
 * <p>
 * The invalid value set in the Clova Application can be easily detected by setting the log level to ERROR.
 */
@LogLevel(ERROR)
public class InvalidApplicationParameterException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs an exception with the specified detail message.
     *
     * @param message detail message
     */
    public InvalidApplicationParameterException(String message) {
        super(message);
    }

    /**
     * Constructs an exception with the specified detail message and the cause.
     *
     * @param message detail message
     * @param cause   cause
     */
    public InvalidApplicationParameterException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs an exception.
     * <p>
     * This method is for subclasses.
     */
    protected InvalidApplicationParameterException() {
        super((String) null);
    }

}
