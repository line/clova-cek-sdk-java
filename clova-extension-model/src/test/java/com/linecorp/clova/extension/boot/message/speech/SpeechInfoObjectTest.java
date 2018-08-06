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

import static com.linecorp.clova.extension.boot.message.speech.SpeechInfoObject.Type.PLAIN_TEXT;
import static com.linecorp.clova.extension.boot.message.speech.SpeechInfoObject.Type.URL;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;
import java.util.UUID;

import org.junit.Test;
import org.springframework.context.i18n.LocaleContextHolder;

import com.linecorp.clova.extension.boot.message.speech.SpeechInfoObject.Lang;

public class SpeechInfoObjectTest {

    @Test
    public void text() throws Exception {
        String text = UUID.randomUUID().toString();
        Locale locale = Locale.UK;
        LocaleContextHolder.setLocale(locale);
        assertThat(SpeechInfoObject.text(text)).satisfies(speechInfoObject -> {
            assertThat(speechInfoObject.getValue()).isEqualTo(text);
            assertThat(speechInfoObject.getLang()).isEqualTo(Lang.identifyBy(locale));
            assertThat(speechInfoObject.getType()).isEqualTo(PLAIN_TEXT);
        });
    }

    @Test
    public void textAndLocale() throws Exception {
        String text = UUID.randomUUID().toString();
        Locale locale = Locale.CHINA;
        LocaleContextHolder.setLocale(Locale.UK);
        assertThat(SpeechInfoObject.text(text, locale)).satisfies(speechInfoObject -> {
            assertThat(speechInfoObject.getValue()).isEqualTo(text);
            assertThat(speechInfoObject.getLang()).isEqualTo(Lang.identifyBy(locale));
            assertThat(speechInfoObject.getType()).isEqualTo(PLAIN_TEXT);
        });
    }

    @Test
    public void url() throws Exception {
        String url = UUID.randomUUID().toString();
        LocaleContextHolder.setLocale(Locale.US);
        assertThat(SpeechInfoObject.url(url)).satisfies(speechInfoObject -> {
            assertThat(speechInfoObject.getValue()).isEqualTo(url);
            assertThat(speechInfoObject.getLang()).isEqualTo(Lang.NONE);
            assertThat(speechInfoObject.getType()).isEqualTo(URL);
        });
    }

}
