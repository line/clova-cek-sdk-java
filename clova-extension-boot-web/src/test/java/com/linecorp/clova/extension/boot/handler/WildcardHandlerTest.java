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

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.linecorp.clova.extension.boot.controller.advice.CEKHandleIntentControllerAdvice;
import com.linecorp.clova.extension.boot.handler.annnotation.CEKRequestHandler;
import com.linecorp.clova.extension.boot.handler.annnotation.EventMapping;
import com.linecorp.clova.extension.boot.handler.annnotation.IntentMapping;
import com.linecorp.clova.extension.boot.message.response.CEKResponse;
import com.linecorp.clova.extension.test.CEKRequestGenerator;

import lombok.extern.slf4j.Slf4j;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("WildcardHandlerTest")
public class WildcardHandlerTest {

    @TestConfiguration
    @Profile("WildcardHandlerTest")
    static class TestConfig {

        @CEKRequestHandler
        @Slf4j
        static class TestHandler {

            @IntentMapping("*")
            CEKResponse handleIntent() {
                return CEKResponse.empty();
            }

            @EventMapping("*")
            CEKResponse handleEvent() {
                return CEKResponse.empty();
            }

        }
    }

    @Autowired
    MockMvc mvc;

    @SpyBean
    TestConfig.TestHandler handler;

    @SpyBean
    CEKHandleIntentControllerAdvice exceptionHandler;

    @Before
    public void setUp() {
        reset(handler, exceptionHandler);
    }

    @Test
    public void handleIntent() throws Exception {
        String body = CEKRequestGenerator.requestBodyBuilder()
                                         .intent(RandomStringUtils.randomAlphabetic(10))
                                         .build();

        mvc.perform(post("/cek/v1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
           .andDo(print())
           .andExpect(status().isOk());

        verify(this.handler).handleIntent();
    }

    @Test
    public void handleEvent() throws Exception {
        String body = CEKRequestGenerator.requestBodyBuilder()
                                         .event(RandomStringUtils.randomAlphabetic(10)
                                                + "." + RandomStringUtils.randomAlphabetic(10))
                                         .build();

        mvc.perform(post("/cek/v1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
           .andDo(print())
           .andExpect(status().isOk());

        verify(this.handler).handleEvent();
    }

}
