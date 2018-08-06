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

/**
 * An abstract annotation that specifies the parameters of the CEK request.
 * <p>
 * It properly binds a parameter of the CEK request by specifying it with argument of handler method.
 *
 * @see ContextValue
 * @see SlotValue
 * @see SessionValue
 */
@Target({ ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CEKRequestParam {

    /**
     * The parameter name to bind.
     * <p>
     * By default, a parameter matching the argument name of the HandlerMethod is bound.
     * Also, even if the case formats of the argument name and the parameter name do not match, they are automatically converted to lower camel is bound.
     * <p>
     * Supported case formats are as follows.
     * <ul>
     * <li>Lower/Upper Kebab
     * <li>Lower/Upper Snake
     * <li>Pascal
     * </ul>
     * Note that this is not the case when a parameter name is specified with this annotation.
     */
    String value() default "";

    /**
     * Whether {@link #value() the parameter name} is required.
     */
    boolean required() default true;

}
