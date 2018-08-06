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

import static com.fasterxml.jackson.annotation.JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY;
import static com.fasterxml.jackson.annotation.JsonFormat.Feature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static lombok.AccessLevel.PRIVATE;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;

import javax.validation.Valid;

import org.springframework.context.i18n.LocaleContextHolder;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Singular;
import lombok.ToString;

/**
 * An object that contains information to be synthesized as a voice.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = PRIVATE)
@ToString(doNotUseGetters = true)
public class OutputSpeech implements Serializable {

    private static final long serialVersionUID = 1L;

    @Valid
    @JsonInclude(NON_NULL)
    private SpeechInfoObject brief;
    @Valid
    @Singular
    @JsonInclude(NON_EMPTY)
    @JsonFormat(with = { ACCEPT_SINGLE_VALUE_AS_ARRAY, WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED })
    private List<SpeechInfoObject> values;
    @Valid
    @JsonInclude(NON_NULL)
    private Verbose verbose;

    /**
     * Alternative to {@link #text(String, Locale)} using a code only.
     * <p>
     * The locale is for {@link SpeechInfoObject#lang} chosen from {@link LocaleContextHolder}.
     *
     * @param text {@link SpeechInfoObject#value}
     * @return New instance of this class
     */
    public static OutputSpeech text(String text) {
        return text(text, LocaleContextHolder.getLocale());
    }

    /**
     * Constructs new instance with the specified single text and the locale.
     *
     * @param text   {@link SpeechInfoObject#value}
     * @param locale to choose {@link SpeechInfoObject#lang}
     * @return New instance of this class
     */
    public static OutputSpeech text(String text, Locale locale) {
        return OutputSpeech.builder()
                           .value(SpeechInfoObject.text(text, locale))
                           .build();
    }

    /**
     * Constructs new instance with the specified single url.
     *
     * @param url {@link SpeechInfoObject#value}
     * @return New instance of this class
     */
    public static OutputSpeech url(String url) {
        return OutputSpeech.builder()
                           .value(SpeechInfoObject.url(url))
                           .build();
    }

    /**
     * The type of voice information to output.
     * <dl>
     * <dt>SimpleSpeech</dt>
     * <dd>Voice information in a simple sentence format. This is the most basic type,
     * and the response.outputSpeech.values field must have a SpeechInfoObject object if this value is designated.</dd>
     * <dt>SpeechList</dt>
     * <dd>Voice information with a complex sentence type. This is used when outputting many sentences.
     * If this value is designated, the response.outputSpeech.values field must have SpeechInfoObject object array.</dd>
     * <dt>SpeechSet</dt>
     * <dd>Voice information in a combined format. This is used to deliver the summary of the voice information
     * and detailed voice information to a client device without a display. If this value is designated,
     * it must have the response.outputSpeech.brief and response.outputSpeech.verbose fields instead of the response.outputSpeech.values field.</dd>
     * </dl>
     */
    @RequiredArgsConstructor
    public enum Type {
        SIMPLE_SPEECH("SimpleSpeech"),
        SPEECH_LIST("SpeechList"),
        SPEECH_SET("SpeechSet");

        @Getter(onMethod = @__(@JsonValue))
        private final String value;
    }

    /**
     * Return the type of voice information.
     *
     * @return the type of voice information
     */
    public Type getType() {
        if (brief != null && verbose != null) {
            return Type.SPEECH_SET;
        }
        if (brief == null && verbose == null && values != null && values.size() > 0) {
            if (values.size() == 1) {
                return Type.SIMPLE_SPEECH;
            }
            return Type.SPEECH_LIST;
        }
        throw new IllegalStateException("OutputSpeech is invalid structure. [outputSpeech:[" + this + "]]");
    }

}
