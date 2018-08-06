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

package com.linecorp.clova.extension.boot.message.speech;

/**
 * Strategy interface used to resolve message code for {@link org.springframework.context.MessageSource}.
 * <p>
 * Users of {@link OutputSpeechGenerator} should prepare a message via MessageSource
 * and implement {@link MessageCode#getCode() MessageCode.getCode()} method. Calling
 * {@link OutputSpeechGenerator#generateFrom(MessageCode, Object...)} will resolve messages from a MessageCode object.
 * <p>
 * Below is an example of a MessageCode and generator that generates OutputSpeech via 'Foo' object.<br>
 * To start, implement the MessageCode interface and override the getCode method:
 * <pre><code>
 * class Foo implements MessageCode {
 *   &#64;Override
 *   String getCode() {
 *       return "foo.bar"
 *   }
 * }
 * </code></pre>
 * Next, prepare a message resource, including code with a ".brief" prefix.
 * <pre><code>
 *   foo.bar.brief=some messages for presentation
 * </code></pre>
 * You can get message via the OutputSpeechGenerator#generateFrom with a Foo object.
 * <pre><code>
 *   String message = outputSpeechGenerator.generateFrom(new Foo()); // "some message for presentation"
 * </code></pre>
 */
public interface MessageCode {

    /**
     * Try to resolve the message code for {@link org.springframework.context.MessageSource}.
     *
     * @return message code
     */
    String getCode();
}
