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

package com.linecorp.clova.extension.boot.handler.annnotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.linecorp.clova.extension.boot.message.request.CEKRequest;
import com.linecorp.clova.extension.boot.message.request.RequestType;

/**
 * An annotation for mapping a CEK request with the request processing {@link CEKRequestHandler handler}.
 *
 * @see EventMapping
 * @see IntentMapping
 * @see LaunchMapping
 * @see SessionEndedMapping
 */
@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CEKRequestMapping {

    /**
     * The request type.
     */
    RequestType type();

    /**
     * The request name to handle.
     * <p>
     * This value is matched with {@link CEKRequest#getName()}.
     */
    String value() default "";

    /**
     * Whether {@link #value() a request} name is required.
     */
    boolean nameRequired() default true;
}
