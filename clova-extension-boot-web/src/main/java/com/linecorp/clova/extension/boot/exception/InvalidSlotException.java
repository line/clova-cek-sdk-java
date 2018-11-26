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

import org.springframework.core.MethodParameter;

import com.linecorp.clova.extension.boot.message.request.Slot;
import com.linecorp.clova.extension.boot.util.StringUtils;

import lombok.Getter;

/**
 * An exception when an invalid slot is set.
 */
public class InvalidSlotException extends InvalidApplicationParameterException {

    @Getter
    private final Slot<?> slot;
    @Getter
    private final MethodParameter methodParam;

    /**
     * Constructs an exception with the given slot and method parameter.
     *
     * @param slot        slot that is failed to set into given method parameter
     * @param methodParam method parameter that given slot was supposed to be set
     * @param t           an exception that occurs when trying to convert the slot value to the type of method
     *                    parameter
     */
    public InvalidSlotException(Slot<?> slot, MethodParameter methodParam, Throwable t) {
        super(null, t);
        this.slot = slot;
        this.methodParam = methodParam;
    }

    @Override
    public String getMessage() {
        return "Failed to mapping. [" + slot + " -> " + StringUtils.methodParamToString(methodParam) + "]";
    }

}
