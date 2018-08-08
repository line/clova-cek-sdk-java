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

package com.linecorp.clova.extension.boot.controller.advice;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;

import com.linecorp.clova.extension.boot.handler.annnotation.CEKRequestHandler;
import com.linecorp.clova.extension.boot.handler.annnotation.IntentMapping;
import com.linecorp.clova.extension.boot.handler.annnotation.SlotValue;
import com.linecorp.clova.extension.boot.message.response.CEKResponse;
import com.linecorp.clova.extension.test.CEKRequestGenerator;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class CEKSDKInformationBinderResponseBodyAdviceTest {

    @TestConfiguration
    static class TestConfig {

        @CEKRequestHandler
        static class TestHandler {

            @IntentMapping("SampleIntent")
            CEKResponse handle(@SlotValue String query) {
                return CEKResponse.empty();
            }

        }

    }

    @Autowired
    MockMvc mvc;

    @SpyBean
    TestConfig.TestHandler handler;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    @SuppressWarnings("SpringJavaAutowiringInspection")
    Configuration configuration;

    @Test
    public void testCorrectResponse() throws Exception {
        String query = RandomStringUtils.randomAlphabetic(10);

        mvc.perform(post("/cek/v1")
                            .content(CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                                        .intent("SampleIntent")
                                                        .slot("query", query)
                                                        .build())
                            .contentType(MediaType.APPLICATION_JSON))
           .andDo(print())
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.meta.customExtensionSdk")
                              .value(CEKSDKInformationBinderResponseBodyAdvice.CEK_SDK_NAME))
           .andExpect(jsonPath("$.meta.customExtensionSdkVersion")
                              .value("1.0.0"));

        verify(handler).handle(eq(query));
    }

    @Test
    public void testErrorResponse() throws Exception {
        mvc.perform(post("/cek/v1")
                            .content(CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                                        .intent("SampleIntent")
                                                        .build())
                            .contentType(MediaType.APPLICATION_JSON))
           .andDo(print())
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.meta.customExtensionSdk")
                              .value(CEKSDKInformationBinderResponseBodyAdvice.CEK_SDK_NAME))
           .andExpect(jsonPath("$.meta.customExtensionSdkVersion")
                              .value("1.0.0"));

        verify(handler, never()).handle(any());
    }

}
