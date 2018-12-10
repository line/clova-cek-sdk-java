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
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.junit.After;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import com.jayway.jsonpath.Configuration;

import com.linecorp.clova.extension.boot.controller.advice.CEKHandleIntentControllerAdvice;
import com.linecorp.clova.extension.boot.exception.RequestHandlerNotFoundException;
import com.linecorp.clova.extension.boot.handler.annnotation.CEKRequestHandler;
import com.linecorp.clova.extension.boot.handler.annnotation.ExtensionIdCondition;
import com.linecorp.clova.extension.boot.handler.annnotation.IntentMapping;
import com.linecorp.clova.extension.boot.message.response.CEKResponse;
import com.linecorp.clova.extension.test.CEKRequestGenerator;

import lombok.extern.slf4j.Slf4j;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
@ActiveProfiles("MultipleExtensionHandlerTest")
public class MultipleExtensionHandlerTest {

    @TestConfiguration
    @Profile("MultipleExtensionHandlerTest")
    static class TestConfig {

        @FooRequestHandler
        static class FooExtensionHandler {

            @IntentMapping("Multi.FooBar")
            CEKResponse handleFooBar() {
                return CEKResponse.empty();
            }

        }

        @BarRequestHandler
        static class BarExtensionHandler {

            @IntentMapping("Multi.FooBar")
            CEKResponse handleFooBar() {
                return CEKResponse.empty();
            }

            @IntentMapping("Multi.BarOnly")
            CEKResponse handleBarOnly() {
                return CEKResponse.empty();
            }
        }

        @CEKRequestHandler
        static class ExtensionsCommonHandler {

            @IntentMapping("Multi.FooBar")
            CEKResponse handleFooBar() {
                return CEKResponse.empty();
            }

            @IntentMapping("Multi.Common")
            CEKResponse handleCommon() {
                return CEKResponse.empty();
            }

        }

    }

    @Autowired
    MockMvc mvc;
    @Autowired
    Configuration configuration;

    @SpyBean
    TestConfig.FooExtensionHandler fooHandler;
    @SpyBean
    TestConfig.BarExtensionHandler barHandler;
    @SpyBean
    TestConfig.ExtensionsCommonHandler commonHandler;

    @SpyBean
    CEKHandleIntentControllerAdvice exceptionHandler;

    @After
    public void tearDown() {
        reset(fooHandler, barHandler, commonHandler, exceptionHandler);
    }

    @Test
    public void handleFooBar_foo() throws Exception {
        callHandlerBy("Multi.FooBar", "foo");

        verify(fooHandler).handleFooBar();
    }

    @Test
    public void handleBarOnly_foo() throws Exception {
        MvcResult mvcResult = callHandlerBy("Multi.BarOnly", "foo")
                .andExpect(jsonPath("$.response.outputSpeech.values.value").isNotEmpty())
                .andReturn();

        verify(this.exceptionHandler).handle(
                isA(RequestHandlerNotFoundException.class),
                eq(mvcResult.getRequest()));
    }

    @Test
    public void handleCommon_foo() throws Exception {
        callHandlerBy("Multi.Common", "foo");

        verify(commonHandler).handleCommon();
    }

    @Test
    public void handleFooBar_bar() throws Exception {
        callHandlerBy("Multi.FooBar", "bar");

        verify(barHandler).handleFooBar();
    }

    @Test
    public void handleBarOnly_bar() throws Exception {
        callHandlerBy("Multi.BarOnly", "bar");

        verify(barHandler).handleBarOnly();
    }

    @Test
    public void handleCommon_bar() throws Exception {
        callHandlerBy("Multi.Common", "bar");

        verify(commonHandler).handleCommon();
    }

    @Test
    public void handleFooBar_hoge() throws Exception {
        callHandlerBy("Multi.FooBar", "hoge");

        verify(commonHandler).handleFooBar();
    }

    @Test
    public void handleCommon_hoge() throws Exception {
        callHandlerBy("Multi.Common", "hoge");

        verify(commonHandler).handleCommon();
    }

    @Test
    public void handleBarOnly_hoge() throws Exception {
        MvcResult mvcResult = callHandlerBy("Multi.BarOnly", "hoge")
                .andExpect(jsonPath("$.response.outputSpeech.values.value").isNotEmpty())
                .andReturn();

        verify(this.exceptionHandler).handle(
                isA(RequestHandlerNotFoundException.class),
                eq(mvcResult.getRequest()));
    }

    private ResultActions callHandlerBy(String intent, String extensionId) throws Exception {
        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent(intent)
                                         .put("$.context.System.application.applicationId", extensionId)
                                         .build();

        return mvc.perform(post("/cek/v1")
                                   .content(body)
                                   .contentType(MediaType.APPLICATION_JSON))
                  .andDo(print())
                  .andExpect(status().isOk());
    }

    @Retention(RetentionPolicy.RUNTIME)
    @CEKRequestHandler
    @ExtensionIdCondition("foo")
    @interface FooRequestHandler {}

    @Retention(RetentionPolicy.RUNTIME)
    @CEKRequestHandler
    @ExtensionIdCondition("bar")
    @interface BarRequestHandler {}

}
