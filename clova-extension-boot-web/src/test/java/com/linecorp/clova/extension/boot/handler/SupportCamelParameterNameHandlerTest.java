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

package com.linecorp.clova.extension.boot.handler;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.linecorp.clova.extension.boot.handler.annnotation.CEKRequestHandler;
import com.linecorp.clova.extension.boot.handler.annnotation.ContextValue;
import com.linecorp.clova.extension.boot.handler.annnotation.IntentMapping;
import com.linecorp.clova.extension.boot.handler.annnotation.SlotValue;
import com.linecorp.clova.extension.boot.message.context.AudioPlayerContext;
import com.linecorp.clova.extension.boot.message.response.CEKResponse;
import com.linecorp.clova.extension.test.CEKRequestGenerator;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class SupportCamelParameterNameHandlerTest {

    @TestConfiguration
    static class TestConfig {

        @CEKRequestHandler
        static class TestHandler {

            @IntentMapping("IntentOtherName")
            CEKResponse handleOtherName(@ContextValue("AudioPlayer") AudioPlayerContext player,
                                        @SlotValue("audio_query") String query) {
                return CEKResponse.empty();
            }

            @IntentMapping("IntentToCamel")
            CEKResponse handleCamel(@SlotValue String audioQuery) {
                return CEKResponse.empty();
            }

        }

    }

    @Autowired
    MockMvc mvc;

    @SpyBean
    TestConfig.TestHandler handler;

    @Captor
    ArgumentCaptor<AudioPlayerContext> audioPlayerCaptor;

    @Before
    public void setUp() {
        reset(handler);
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void handleOtherName() throws Exception {
        String query = RandomStringUtils.randomAlphanumeric(100);

        AudioPlayerContext audioPlayer = CEKRequestGenerator.createContext(AudioPlayerContext.class);

        String body = CEKRequestGenerator.requestBodyBuilder()
                                         .context("AudioPlayer", audioPlayer)
                                         .intent("IntentOtherName")
                                         .slot("audio_query", query)
                                         .build();

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON))
           .andDo(print())
           .andExpect(status().isOk());

        verify(handler).handleOtherName(eq(audioPlayer), eq(query));
    }

    @Test
    public void handleCamelToCamel() throws Exception {
        String expectedQuery = RandomStringUtils.randomAlphanumeric(100);

        String body = CEKRequestGenerator.requestBodyBuilder()
                                         .intent("IntentToCamel")
                                         .slot("audioQuery", expectedQuery)
                                         .build();

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON))
           .andDo(print())
           .andExpect(status().isOk());

        verify(handler).handleCamel(eq(expectedQuery));
    }

    @Test
    public void handlePascalToCamel() throws Exception {
        String expectedQuery = RandomStringUtils.randomAlphanumeric(100);

        String body = CEKRequestGenerator.requestBodyBuilder()
                                         .intent("IntentToCamel")
                                         .slot("AudioQuery", expectedQuery)
                                         .build();

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON))
           .andDo(print())
           .andExpect(status().isOk());

        verify(handler).handleCamel(eq(expectedQuery));
    }

    @Test
    public void handleLowerSnakeToCamel() throws Exception {
        String expectedQuery = RandomStringUtils.randomAlphanumeric(100);

        String body = CEKRequestGenerator.requestBodyBuilder()
                                         .intent("IntentToCamel")
                                         .slot("audio_query", expectedQuery)
                                         .build();

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON))
           .andDo(print())
           .andExpect(status().isOk());

        verify(handler).handleCamel(eq(expectedQuery));
    }

    @Test
    public void handleUpperSnakeToCamel() throws Exception {
        String expectedQuery = RandomStringUtils.randomAlphanumeric(100);

        String body = CEKRequestGenerator.requestBodyBuilder()
                                         .intent("IntentToCamel")
                                         .slot("AUDIO_QUERY", expectedQuery)
                                         .build();

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON))
           .andDo(print())
           .andExpect(status().isOk());

        verify(handler).handleCamel(eq(expectedQuery));
    }

    @Test
    public void handleLowerKebabToCamel() throws Exception {
        String expectedQuery = RandomStringUtils.randomAlphanumeric(100);

        String body = CEKRequestGenerator.requestBodyBuilder()
                                         .intent("IntentToCamel")
                                         .slot("audio-query", expectedQuery)
                                         .build();

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON))
           .andDo(print())
           .andExpect(status().isOk());

        verify(handler).handleCamel(eq(expectedQuery));
    }

    @Test
    public void handleUpperKebabToCamel() throws Exception {
        String expectedQuery = RandomStringUtils.randomAlphanumeric(100);

        String body = CEKRequestGenerator.requestBodyBuilder()
                                         .intent("IntentToCamel")
                                         .slot("AUDIO-QUERY", expectedQuery)
                                         .build();

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON))
           .andDo(print())
           .andExpect(status().isOk());

        verify(handler).handleCamel(eq(expectedQuery));
    }

}
