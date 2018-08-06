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

package com.linecorp.clova.extension.boot.message.response;

import static lombok.AccessLevel.PRIVATE;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.linecorp.clova.extension.boot.message.directive.Directive;
import com.linecorp.clova.extension.boot.message.speech.OutputSpeech;
import com.linecorp.clova.extension.boot.message.speech.Reprompt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

/**
 * An object containing the extension's response information.
 * <p>
 * Users of ExtensionBoot typically write handler classes using {@code CEKRequestHandler} annotation.
 * These classes should return a CEKResponse instance.
 * Below is an example on how to use the builder method.
 * <pre><code>
 * &#64;IntentMappling("Clova.Foo")
 * CEKResponse handleFooIntent() {
 *    return CEKResponse.builder()
 *      .directive(PlaybackControllerDirectives.pause())
 *      .build();
 * }
 * </code></pre>
 */
@Data
@Builder
@AllArgsConstructor(access = PRIVATE)
public class CEKResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final CEKResponse EMPTY = new CEKResponse();

    @NotNull
    @Builder.Default
    private Object card = Collections.emptyMap();
    @Valid
    private OutputSpeech outputSpeech;
    @Valid
    private Reprompt reprompt;

    @NotNull
    @Valid
    @Singular
    private List<Directive> directives;
    @NotNull
    @Builder.Default
    private Boolean shouldEndSession = Boolean.TRUE;

    public CEKResponse() {
        this.card = Collections.emptyMap();
        this.directives = Collections.emptyList();
        this.shouldEndSession = Boolean.TRUE;
    }

    /**
     * Return a minimal valid empty response object.
     * <p>
     * If the extension returns a blank object, the CIC processes it as an incorrect response.
     * Therefore, the extension should return a minimal valid JSON object.
     * <p>
     * This method makes a minimal valid JSON object.
     *
     * @return emtpy cek response.
     */
    public static CEKResponse empty() {
        return EMPTY;
    }

}
