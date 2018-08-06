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
 * An annotation indicating that the context is included in the CEK request.
 * <p>
 * Consider a CEK Request with an AudioPlayer in context as shown below.
 * <pre><code>
 * "context":{
 *   "AudioPlayer": {
 *      "offsetInMilliseconds": 300000
 *      // other properties...
 *   }
 * }
 * </code></pre>
 * First define a class, such as those outlined below, that has implemented the ContextProperty in order to acquire
 * the context value of the aforementioned AudioPlayer.
 * <pre><code>
 * &#64;Data
 * public class AudioPlayer implements ContextProperty {
 *   private Long offsetInMilliseconds;
 *   // other fields...
 * }
 * </code></pre>
 * Next, indicate the AudioPlayer class defined in the Handler method's arguments as outlined below.
 * <pre><code>
 * &#64;IntentMapping("Example.Hoge")
 * public CEKResponse handleHoge(@ContextValue(required = false) AudioPlayer foo) {
 *   System.out.println("offsetInMilliseconds value: " + foo.offsetInMilliseconds);
 *   // =&gt; offsetInMilliseconds value: 300000
 * }
 * </code></pre>
 */
@Target({ ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@CEKRequestParam
public @interface ContextValue {

    /**
     * The context name.
     *
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
