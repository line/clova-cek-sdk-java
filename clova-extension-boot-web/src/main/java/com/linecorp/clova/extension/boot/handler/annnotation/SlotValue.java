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

import org.springframework.core.annotation.AliasFor;

/**
 * An annotation for performing mapping of the slots value.
 * <p>
 * When the IntentRequest slots are as follows:
 * <pre><code>
 * "slots":{
 *   "foo": {
 *     "name": "foo",
 *     "value": "bar"
 *   }
 * }
 * </code></pre>
 * The Handler method will map slots as follows.
 * <pre><code>
 * &#64;IntentMapping("Example.Hoge")
 * public CEKResponse handleHoge(@SlotValue String foo) {
 *   System.out.println("foo value: " + foo);
 *   // =&gt; foo value: bar
 * }
 * </code></pre>
 */
@Target({ ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@CEKRequestParam
public @interface SlotValue {

    /**
     * @see CEKRequestParam#value()
     */
    @AliasFor(annotation = CEKRequestParam.class)
    String value() default "";

    /**
     * @see CEKRequestParam#required()
     */
    @AliasFor(annotation = CEKRequestParam.class)
    boolean required() default true;
}
