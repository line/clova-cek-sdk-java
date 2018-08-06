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

package com.linecorp.clova.extension.boot.message;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

import java.util.Locale;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.test.context.junit4.SpringRunner;

import com.linecorp.clova.extension.boot.exception.InvalidUserParameterException;
import com.linecorp.clova.extension.boot.exception.MissingSlotException;
import com.linecorp.clova.extension.boot.message.speech.OutputSpeech;
import com.linecorp.clova.extension.boot.message.speech.OutputSpeechGenerator;
import com.linecorp.clova.extension.boot.message.speech.SpeechInfoObject;
import com.linecorp.clova.extension.boot.message.speech.Verbose;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = NONE)
@AutoConfigureMockMvc
public class OutputSpeechGeneratorTest {

    @Autowired
    OutputSpeechGenerator outputSpeechGenerator;

    @Before
    public void setup() {
        LocaleContextHolder.setLocale(Locale.JAPAN, true);
    }

    @Test
    public void generateFrom() {
        Assertions.assertThat(outputSpeechGenerator.generateFrom(new MissingSlotException(""))).satisfies(
                outputSpeech -> {
                    Assertions.assertThat(outputSpeech.getBrief()).isNull();
                    Assertions.assertThat(outputSpeech.getType()).isEqualTo(OutputSpeech.Type.SIMPLE_SPEECH);
                    Assertions.assertThat(outputSpeech.getVerbose()).isNull();
                    Assertions.assertThat(outputSpeech.getValues()).hasSize(1)
                              .allSatisfy(value -> {
                                  Assertions.assertThat(value.getValue()).isEqualTo("サーバーに一時的な問題が発生しました。");
                                  Assertions.assertThat(value.getType()).isEqualTo(
                                          SpeechInfoObject.Type.PLAIN_TEXT);
                                  Assertions.assertThat(value.getLang()).isEqualTo(SpeechInfoObject.Lang.JA);
                              });
                });

        Assertions.assertThat(outputSpeechGenerator.generateFrom(new Throwable())).satisfies(outputSpeech -> {
            Assertions.assertThat(outputSpeech.getBrief()).isNull();
            Assertions.assertThat(outputSpeech.getType()).isEqualTo(OutputSpeech.Type.SIMPLE_SPEECH);
            Assertions.assertThat(outputSpeech.getVerbose()).isNull();
            Assertions.assertThat(outputSpeech.getValues()).hasSize(1)
                      .allSatisfy(value -> {
                          Assertions.assertThat(value.getValue()).isEqualTo(
                                  "サーバーに一時的な問題が発生しました。しばらくしてからもう一度お試しください。");
                          Assertions.assertThat(value.getType()).isEqualTo(SpeechInfoObject.Type.PLAIN_TEXT);
                          Assertions.assertThat(value.getLang()).isEqualTo(SpeechInfoObject.Lang.JA);
                      });
        });

        Assertions.assertThat(outputSpeechGenerator.generateFrom(new InvalidUserParameterException("")))
                  .satisfies(outputSpeech -> {
                      Assertions.assertThat(outputSpeech.getBrief()).isNotNull()
                                .satisfies(brief -> {
                                    Assertions.assertThat(brief.getValue()).isEqualTo("りかいできませんでした。");
                                    Assertions.assertThat(brief.getType()).isEqualTo(
                                            SpeechInfoObject.Type.PLAIN_TEXT);
                                    Assertions.assertThat(brief.getLang()).isEqualTo(SpeechInfoObject.Lang.JA);
                                });
                      Assertions.assertThat(outputSpeech.getType()).isEqualTo(OutputSpeech.Type.SPEECH_SET);
                      Assertions.assertThat(outputSpeech.getValues()).isNullOrEmpty();
                      Assertions.assertThat(outputSpeech.getVerbose()).isNotNull()
                                .satisfies(verbose -> {
                                    Assertions.assertThat(verbose.getType()).isEqualTo(
                                            Verbose.Type.SPEECH_LIST);
                                    Assertions.assertThat(verbose.getValues())
                                              .allSatisfy(value -> {
                                                  Assertions.assertThat(value.getLang()).isEqualTo(
                                                          SpeechInfoObject.Lang.JA);
                                                  Assertions.assertThat(value.getType()).isEqualTo(
                                                          SpeechInfoObject.Type.PLAIN_TEXT);
                                              })
                                              .extracting(SpeechInfoObject::getValue)
                                              .containsExactly("理解できませんでした。", "あとでもう一度お試しください。");
                                });
                  });
    }
}
