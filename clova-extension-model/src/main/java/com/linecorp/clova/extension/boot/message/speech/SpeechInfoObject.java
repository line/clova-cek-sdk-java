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

import static lombok.AccessLevel.PRIVATE;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.context.i18n.LocaleContextHolder;

import com.fasterxml.jackson.annotation.JsonValue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

/**
 * The object reused in the response.outputSpeech of a response message.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = PRIVATE)
public class SpeechInfoObject implements Serializable {

    private static final long serialVersionUID = 1L;

    private Lang lang;
    @NotNull
    private Type type;
    @NotBlank
    private String value;

    /**
     * Alternative to {@link #text(String, Locale)} using a code only.
     * <p>
     * The locale is for {@link #lang} chosen from {@link LocaleContextHolder}.
     *
     * @param text {@link #value}
     * @return New instance of this class
     */
    public static SpeechInfoObject text(String text) {
        return text(text, LocaleContextHolder.getLocale());
    }

    /**
     * Constructs new instance with the specified text and the locale.
     *
     * @param text   {@link #value}
     * @param locale to choose {@link #lang}
     * @return New instance of this class
     */
    public static SpeechInfoObject text(String text, Locale locale) {
        return SpeechInfoObject.builder()
                               .lang(Lang.identifyBy(locale))
                               .value(text)
                               .type(Type.PLAIN_TEXT)
                               .build();
    }

    /**
     * Constructs new instance with the specified url.
     *
     * @param url {@link #value}
     * @return New instance of this class
     */
    public static SpeechInfoObject url(String url) {
        return SpeechInfoObject.builder()
                               .value(url)
                               .type(Type.URL)
                               .build();
    }

    /**
     * Code of the language to use for voice synthesis.
     */
    @RequiredArgsConstructor
    public enum Lang {
        /**
         * English
         */
        EN("en", Locale.ENGLISH),
        /**
         * Korean
         */
        KO("ko", Locale.KOREAN),
        /**
         * Japanese
         */
        JA("ja", Locale.JAPANESE),

        NONE("", null);

        @Getter(onMethod = @__(@JsonValue))
        private final String value;
        private final Locale locale;

        /**
         * Return code from {@link Locale}
         *
         * @param locale from system, Spring property or anything else.
         * @return the code from locale
         */
        public static Lang identifyBy(Locale locale) {
            if (locale == null) {
                return Lang.NONE;
            }
            return Arrays.stream(values())
                         .filter(lang -> lang.locale != null)
                         .filter(lang -> Objects.equals(lang.locale.getLanguage(), locale.getLanguage()))
                         .findFirst()
                         .orElse(Lang.NONE);
        }
    }

    /**
     * Type of voice to use.
     */
    @RequiredArgsConstructor
    public enum Type {
        PLAIN_TEXT("PlainText"),
        URL("URL");

        @Getter(onMethod = @__(@JsonValue))
        private final String value;
    }

    /**
     * Return language
     *
     * @return language
     */
    @NotNull
    public Lang getLang() {
        if (this.type == Type.URL) {
            return Lang.NONE;
        }
        return Optional.ofNullable(this.lang)
                       .orElseGet(() -> Lang.identifyBy(LocaleContextHolder.getLocale()));
    }

}
