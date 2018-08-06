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

import static com.fasterxml.jackson.annotation.JsonFormat.Feature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;

import javax.validation.Valid;

import org.springframework.context.i18n.LocaleContextHolder;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Singular;
import lombok.ToString;

/**
 * This is used when delivering content that contains detailed voice information to a client device without a display.
 */
@Data
@Builder
@ToString(doNotUseGetters = true)
public class Verbose implements Serializable {

    private static final long serialVersionUID = 1L;

    @Valid
    @Singular
    @JsonFormat(with = WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED)
    private List<SpeechInfoObject> values;

    /**
     * Alternative to {@link #text(String, Locale)} using a code only.
     * <p>
     * The locale is for {@link SpeechInfoObject#lang} chosen from {@link LocaleContextHolder}.
     *
     * @param text {@link SpeechInfoObject#value}
     * @return New instance of this class
     */
    public static Verbose text(String text) {
        return text(text, LocaleContextHolder.getLocale());
    }

    /**
     * Constructs new instance with the specified single text and the locale.
     *
     * @param text   {@link SpeechInfoObject#value}
     * @param locale to choose {@link SpeechInfoObject#lang}
     * @return New instance of this class
     */
    public static Verbose text(String text, Locale locale) {
        return Verbose.builder()
                      .value(SpeechInfoObject.text(text, locale))
                      .build();
    }

    /**
     * Constructs new instance with the specified single url.
     *
     * @param url {@link SpeechInfoObject#value}
     * @return New instance of this class
     */
    public static Verbose url(String url) {
        return Verbose.builder()
                      .value(SpeechInfoObject.url(url))
                      .build();
    }

    /**
     * The type of voice information to output.
     */
    @RequiredArgsConstructor
    public enum Type {
        SIMPLE_SPEECH("SimpleSpeech"),
        SPEECH_LIST("SpeechList");

        @Getter(onMethod = @__(@JsonValue))
        private final String value;

    }

    /**
     * Return {@link Type}.
     *
     * @return type
     */
    public Type getType() {
        if (values == null || values.isEmpty()) {
            throw new IllegalStateException("verbose values should not be empty.");
        }
        if (values.size() == 1) {
            return Type.SIMPLE_SPEECH;
        }
        return Type.SPEECH_LIST;
    }

}
