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

import com.linecorp.clova.extension.boot.message.request.CEKRequestMessage;

/**
 * An exception for missing a {@link CEKRequestMessage.Session#sessionAttributes session attribute}.
 * <p>
 * This exception occurred when expected {@link CEKRequestMessage.Session#sessionAttributes session attribute} is missing.
 * Handler methods may access {@link CEKRequestMessage.Session#sessionAttributes session attribute}s with the method parameters.
 * This exception is thrown if the parameter is required.
 *
 * @see CEKRequestMessage
 */
public class MissingSessionAttributeException extends MissingRequiredParamException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs an exception with {@link CEKRequestMessage.Session#sessionAttributes session attribute} name.
     *
     * @param name {@link CEKRequestMessage.Session#sessionAttributes session attribute} name.
     *         This is not different from original one if the method argument specified context property name.
     *         This value is prefer to method argument name.
     */
    public MissingSessionAttributeException(String name) {
        super(name);
    }

    @Override
    public String getMessage() {
        return "Missing session attribute. [attribute name:" + getName() + "]";
    }

}
