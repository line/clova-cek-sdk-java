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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;

import com.linecorp.clova.extension.boot.handler.condition.ExtensionIdConditionMatcherFactory;

/**
 * An annotation for indicating the extension id supported by the Handler.
 * <p>
 * Adding this annotation to the Handler allows you to provide features only to the indicated extension id. For
 * example, the Handler will only match requests for extension id "com.example.foo" by using the indicators
 * outlined below.
 * <pre><code>
 * &#64;CEKRequestHandler
 * &#64;ExtensionIdCondition("com.example.foo")
 * class FooExtensionHandler {
 *   // some handlers
 * }
 * </code></pre>
 * If both Handlers with and without this annotation are defined, the Handler without the annotation will
 * process all requests that do not match the conditions.
 *
 * @see CEKHandleCondition
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@CEKHandleCondition(ExtensionIdConditionMatcherFactory.class)
public @interface ExtensionIdCondition {

    /**
     * Extension ID to handle.
     * <p>
     * Specify non-blank value at least one for either this attribute or {@link #extensionId()}.
     */
    @AliasFor("extensionId")
    String[] value() default {};

    /**
     * Extension ID to handle.
     * <p>
     * Specify non-blank value at least one for either this attribute or {@link #value()}.
     */
    @AliasFor("value")
    String[] extensionId() default {};

}
