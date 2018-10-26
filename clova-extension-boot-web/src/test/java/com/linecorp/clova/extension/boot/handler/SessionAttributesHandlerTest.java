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

import java.util.Optional;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Before;
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

import com.linecorp.clova.extension.boot.handler.annnotation.CEKRequestHandler;
import com.linecorp.clova.extension.boot.handler.annnotation.IntentMapping;
import com.linecorp.clova.extension.boot.handler.annnotation.SessionValue;
import com.linecorp.clova.extension.boot.message.response.CEKResponse;
import com.linecorp.clova.extension.test.CEKRequestGenerator;

import lombok.Data;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class SessionAttributesHandlerTest {

    @Data
    public static class MyData {
        String key1;
        Integer key2;
    }

    @TestConfiguration
    static class TestConfig {

        @CEKRequestHandler
        public static class TestHandler {

            @IntentMapping("StringParam")
            CEKResponse handleStringParam(@SessionValue String param) {
                return CEKResponse.empty();
            }

            @IntentMapping("IntegerParam")
            CEKResponse handleIntegerParam(@SessionValue Integer param) {
                return CEKResponse.empty();
            }

            @IntentMapping("ObjectParam")
            CEKResponse handleObjectParam(@SessionValue MyData param) {
                return CEKResponse.empty();
            }

            @IntentMapping("StringWithKey")
            CEKResponse handleStringWithKey(@SessionValue("param") String someParam) {
                return CEKResponse.empty();
            }

            @IntentMapping("ObjectWithKey")
            CEKResponse handleObjectWithKey(@SessionValue("param") MyData someParam) {
                return CEKResponse.empty();
            }

            @IntentMapping("OptionalObjectWithKey")
            CEKResponse handleOptionalObjectWithKey(@SessionValue("param") Optional<MyData> someParam) {
                return CEKResponse.empty();
            }
        }
    }

    @Autowired
    MockMvc mvc;

    @SpyBean
    TestConfig.TestHandler handler;

    @Before
    public void setUp() {
        reset(handler);
    }

    @Test
    public void handleStringParam() throws Exception {
        String param = RandomStringUtils.randomAlphanumeric(10);

        mvc.perform(post("/cek/v1")
                            .content(CEKRequestGenerator.requestBodyBuilder()
                                                        .intent("StringParam")
                                                        .sessionAttribute("param", param)
                                                        .build())
                            .contentType(MediaType.APPLICATION_JSON))
           .andDo(print())
           .andExpect(status().isOk());

        verify(handler).handleStringParam(eq(param));
    }

    @Test
    public void handleIntegerParam() throws Exception {
        Integer param = RandomUtils.nextInt();
        mvc.perform(post("/cek/v1")
                            .content(CEKRequestGenerator.requestBodyBuilder()
                                                        .intent("IntegerParam")
                                                        .sessionAttribute("param", param)
                                                        .build())
                            .contentType(MediaType.APPLICATION_JSON))
           .andDo(print())
           .andExpect(status().isOk());

        verify(handler).handleIntegerParam(eq(param));
    }

    @Test
    public void handleObjectParam() throws Exception {
        MyData param = new MyData();
        param.setKey1(RandomStringUtils.randomAlphabetic(15));
        param.setKey2(RandomUtils.nextInt());

        mvc.perform(post("/cek/v1")
                            .content(CEKRequestGenerator.requestBodyBuilder()
                                                        .intent("ObjectParam")
                                                        .sessionAttribute("param", param)
                                                        .build())
                            .contentType(MediaType.APPLICATION_JSON))
           .andDo(print())
           .andExpect(status().isOk());

        verify(handler).handleObjectParam(eq(param));
    }

    @Test
    public void handleStringWithKey() throws Exception {
        String param = RandomStringUtils.randomAlphanumeric(20);

        mvc.perform(post("/cek/v1")
                            .content(CEKRequestGenerator.requestBodyBuilder()
                                                        .intent("StringWithKey")
                                                        .sessionAttribute("param", param)
                                                        .build())
                            .contentType(MediaType.APPLICATION_JSON))
           .andDo(print())
           .andExpect(status().isOk());

        verify(handler).handleStringWithKey(eq(param));
    }

    @Test
    public void handleObjectWithKey() throws Exception {
        MyData param = new MyData();
        param.setKey1(RandomStringUtils.randomAlphabetic(15));
        param.setKey2(RandomUtils.nextInt());

        mvc.perform(post("/cek/v1")
                            .content(CEKRequestGenerator.requestBodyBuilder()
                                                        .intent("ObjectWithKey")
                                                        .sessionAttribute("param", param)
                                                        .build())
                            .contentType(MediaType.APPLICATION_JSON))
           .andDo(print())
           .andExpect(status().isOk());

        verify(handler).handleObjectWithKey(eq(param));
    }

    @Test
    public void handleOptionalObjectWithKey() throws Exception {
        MyData param = new MyData();
        param.setKey1(RandomStringUtils.randomAlphabetic(15));
        param.setKey2(RandomUtils.nextInt());

        mvc.perform(post("/cek/v1")
                            .content(CEKRequestGenerator.requestBodyBuilder()
                                                        .intent("OptionalObjectWithKey")
                                                        .sessionAttribute("param", param)
                                                        .build())
                            .contentType(MediaType.APPLICATION_JSON))
           .andDo(print())
           .andExpect(status().isOk());

        verify(handler).handleOptionalObjectWithKey(eq(Optional.of(param)));
    }

    @Test
    public void handleOptionalObjectWithKey_empty() throws Exception {
        mvc.perform(post("/cek/v1")
                            .content(CEKRequestGenerator.requestBodyBuilder()
                                                        .intent("OptionalObjectWithKey")
                                                        .build())
                            .contentType(MediaType.APPLICATION_JSON))
           .andDo(print())
           .andExpect(status().isOk());

        verify(handler).handleOptionalObjectWithKey(eq(Optional.empty()));
    }

}
