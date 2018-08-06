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

import static com.linecorp.clova.extension.boot.message.speech.SpeechInfoObject.text;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.Test;

public class OutputSpeechTest {
    private static final String TEXT1 = UUID.randomUUID().toString();
    private static final String TEXT2 = UUID.randomUUID().toString();

    @Test
    public void testSimpleSpeechBuildTest() {
        OutputSpeech outputSpeech = OutputSpeech.builder()
                                                .value(text(TEXT1))
                                                .build();
        assertThat(outputSpeech.getType()).isEqualTo(OutputSpeech.Type.SIMPLE_SPEECH);
        assertThat(outputSpeech.getVerbose()).isNull();
        assertThat(outputSpeech.getBrief()).isNull();
        assertThat(outputSpeech.getValues())
                .containsOnly(text(TEXT1))
                .hasOnlyOneElementSatisfying(actual -> {
                    assertThat(actual).isEqualTo(text(TEXT1));
                });
    }

    @Test
    public void testSpeechListBuildTest() {
        OutputSpeech outputSpeech = OutputSpeech.builder()
                                                .value(text(TEXT1))
                                                .value(text(TEXT2))
                                                .build();
        assertThat(outputSpeech.getType()).isEqualTo(OutputSpeech.Type.SPEECH_LIST);
        assertThat(outputSpeech.getVerbose()).isNull();
        assertThat(outputSpeech.getBrief()).isNull();
        assertThat(outputSpeech.getValues())
                .hasSize(2)
                .containsSequence(text(TEXT1), text(TEXT2));
    }

    @Test
    public void testSpeechSetBuildTest() {
        OutputSpeech outputSpeech = OutputSpeech.builder()
                                                .brief(text(TEXT1))
                                                .verbose(Verbose.text(TEXT2))
                                                .build();
        assertThat(outputSpeech.getType()).isEqualTo(OutputSpeech.Type.SPEECH_SET);
        assertThat(outputSpeech.getVerbose()).isEqualTo(Verbose.text(TEXT2));
        assertThat(outputSpeech.getBrief()).isEqualTo(text(TEXT1));
        assertThat(outputSpeech.getValues()).isEmpty();
    }
}
