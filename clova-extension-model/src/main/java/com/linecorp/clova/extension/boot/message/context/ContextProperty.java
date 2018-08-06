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

package com.linecorp.clova.extension.boot.message.context;

import java.io.Serializable;

/**
 * An interface of objects included in the CEK request message containing the context information of the client.
 * <p>
 * If you want to access "context" values at your handler, you can use {@code ContextValue}
 * annotation as a handler method parameter.
 * This package provides a "context.System" object as {@link SystemContext}.
 * Other objects are not supported.
 * Therefore, if you want to access other context objects, you should implement {@link ContextProperty}.
 * e.g. You want to access the custom context, you should implement the class like the below sample code.
 * <p>
 * <pre><code>
 * public class CustomContext implements ContextProperty {
 *     private Long customProperty;
 * }
 *
 * &#64;CEKRequestHandler
 * public class FooHandler {
 *     &#64;IntentMapping("Clova.Foo")
 *     CEKResponse handlerFoo(
 *          &#64;ContextValue SystemContext systemContext,
 *          &#64;ContextValue CustomContext customContext
 *     ) {
 *         return CEKResponse.empty();
 *     }
 * }
 * </code></pre>
 */
public interface ContextProperty extends Serializable {
}
