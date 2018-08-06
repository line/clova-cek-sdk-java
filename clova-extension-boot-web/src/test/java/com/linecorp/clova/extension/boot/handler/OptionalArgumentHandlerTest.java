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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

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

import com.jayway.jsonpath.Configuration;

import com.linecorp.clova.extension.boot.handler.annnotation.CEKRequestHandler;
import com.linecorp.clova.extension.boot.handler.annnotation.ContextValue;
import com.linecorp.clova.extension.boot.handler.annnotation.IntentMapping;
import com.linecorp.clova.extension.boot.message.context.ContextProperty;
import com.linecorp.clova.extension.boot.message.response.CEKResponse;
import com.linecorp.clova.extension.test.CEKRequestGenerator;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class OptionalArgumentHandlerTest {

    @TestConfiguration
    static class TestConfig {

        @CEKRequestHandler
        @Slf4j
        static class TestHandler {

            @IntentMapping("HandleContext")
            CEKResponse handleContext(TestContext testContext) {
                return CEKResponse.empty();
            }

            @IntentMapping("HandleNonRequiredContext")
            CEKResponse handleNonRequiredContext(@ContextValue(required = false) TestContext testContext) {
                return CEKResponse.empty();
            }

            @IntentMapping("HandleNonRequiredContextWithValidation")
            CEKResponse handleNonRequiredContextWithValidation(
                    @Valid @ContextValue(required = false) TestContext testContext) {
                return CEKResponse.empty();
            }

            @IntentMapping("HandleOptionalContext")
            CEKResponse handleOptionalContext(Optional<TestContext> testContext) {
                return CEKResponse.empty();
            }

            @IntentMapping("HandleOptionalContextWithValidation")
            CEKResponse handleOptionalContextWithValidation(@Valid Optional<TestContext> testContext) {
                return CEKResponse.empty();
            }

            @IntentMapping("HandleNonRequiredOptionalContext")
            CEKResponse handleNonRequiredOptionalContext(
                    @ContextValue(required = false) Optional<TestContext> testContext) {
                return CEKResponse.empty();
            }
        }
    }

    @Data
    static class TestContext implements ContextProperty {

        private static final long serialVersionUID = 1L;

        @NotBlank
        private String value;
    }

    @Autowired
    MockMvc mvc;

    @SpyBean
    TestConfig.TestHandler handler;

    @Autowired
    @SuppressWarnings("SpringJavaAutowiringInspection")
    Configuration configuration;

    @Before
    public void setUp() {
        reset(handler);
    }

    @Test
    public void handleContext_hasContext() throws Exception {
        TestContext context = new TestContext();
        context.setValue(UUID.randomUUID().toString());

        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("HandleContext")
                                         .put("$.context.TestContext", context)
                                         .build();

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON))
           .andDo(print())
           .andExpect(status().isOk());

        verify(handler).handleContext(eq(context));
    }

    @Test
    public void handleContext_noContext() throws Exception {
        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("HandleContext")
                                         .build();

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON))
           .andDo(print())
           .andExpect(status().isOk());

        verify(handler, never()).handleContext(any());
    }

    @Test
    public void handleNonRequiredContext_hasContext() throws Exception {
        TestContext context = new TestContext();
        context.setValue(UUID.randomUUID().toString());

        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("HandleNonRequiredContext")
                                         .put("$.context.TestContext", context)
                                         .build();

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON))
           .andDo(print())
           .andExpect(status().isOk());

        verify(handler).handleNonRequiredContext(eq(context));
    }

    @Test
    public void handleNonRequiredContext_noContext() throws Exception {
        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("HandleNonRequiredContext")
                                         .build();

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON))
           .andDo(print())
           .andExpect(status().isOk());

        verify(handler).handleNonRequiredContext(isNull());
    }

    @Test
    public void handleNonRequiredContextWithValidation_hasContext_validationOK() throws Exception {
        TestContext context = new TestContext();
        context.setValue(UUID.randomUUID().toString());

        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("HandleNonRequiredContextWithValidation")
                                         .put("$.context.TestContext", context)
                                         .build();

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON))
           .andDo(print())
           .andExpect(status().isOk());

        verify(handler).handleNonRequiredContextWithValidation(eq(context));
    }

    @Test
    public void handleNonRequiredContextWithValidation_hasContext_validationNG() throws Exception {
        TestContext context = new TestContext();

        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("HandleNonRequiredContextWithValidation")
                                         .put("$.context.TestContext", context)
                                         .build();

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON))
           .andDo(print())
           .andExpect(status().isOk());

        verify(handler, never()).handleNonRequiredContextWithValidation(any());
    }

    @Test
    public void handleNonRequiredContextWithValidation_noContext() throws Exception {
        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("HandleNonRequiredContextWithValidation")
                                         .build();

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON))
           .andDo(print())
           .andExpect(status().isOk());

        verify(handler).handleNonRequiredContextWithValidation(isNull());
    }

    @Test
    public void handleOptionalContext_validationOK() throws Exception {
        TestContext context = new TestContext();
        context.setValue(UUID.randomUUID().toString());

        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("HandleOptionalContext")
                                         .put("$.context.TestContext", context)
                                         .build();

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON))
           .andDo(print())
           .andExpect(status().isOk());

        verify(handler).handleOptionalContext(eq(Optional.of(context)));
    }

    @Test
    public void handleOptionalContext_validationNG() throws Exception {
        TestContext context = new TestContext();
        context.setValue("");

        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("HandleOptionalContext")
                                         .put("$.context.TestContext", context)
                                         .build();

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON))
           .andDo(print())
           .andExpect(status().isOk());

        verify(handler).handleOptionalContext(eq(Optional.of(context)));
    }

    @Test
    public void handleOptionalContext_noContext() throws Exception {
        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("HandleOptionalContext")
                                         .build();

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON))
           .andDo(print())
           .andExpect(status().isOk());

        verify(handler).handleOptionalContext(eq(Optional.empty()));
    }

    @Test
    public void handleOptionalContextWithValidation_validationOK() throws Exception {
        TestContext context = new TestContext();
        context.setValue(UUID.randomUUID().toString());

        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("HandleOptionalContextWithValidation")
                                         .put("$.context.TestContext", context)
                                         .build();

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON))
           .andDo(print())
           .andExpect(status().isOk());

        verify(handler).handleOptionalContextWithValidation(eq(Optional.of(context)));
    }

    @Test
    public void handleOptionalContextWithValidation_validationNG() throws Exception {
        TestContext context = new TestContext();
        context.setValue("");

        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("HandleOptionalContextWithValidation")
                                         .put("$.context.TestContext", context)
                                         .build();

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON))
           .andDo(print())
           .andExpect(status().isOk());

        verify(handler, never()).handleOptionalContextWithValidation(any());
    }

    @Test
    public void handleOptionalContextWithValidation_noContext() throws Exception {

        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("HandleOptionalContextWithValidation")
                                         .build();

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON))
           .andDo(print())
           .andExpect(status().isOk());

        verify(handler).handleOptionalContextWithValidation(eq(Optional.empty()));
    }

    @Test
    public void handleNonRequiredOptionalContext_hasContext() throws Exception {
        TestContext context = new TestContext();
        context.setValue(UUID.randomUUID().toString());

        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("HandleNonRequiredOptionalContext")
                                         .put("$.context.TestContext", context)
                                         .build();

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON))
           .andDo(print())
           .andExpect(status().isOk());

        verify(handler).handleNonRequiredOptionalContext(eq(Optional.of(context)));
    }

    @Test
    public void handleNonRequiredOptionalContext_noContext() throws Exception {

        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("HandleNonRequiredOptionalContext")
                                         .build();

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON))
           .andDo(print())
           .andExpect(status().isOk());

        verify(handler).handleNonRequiredOptionalContext(eq(Optional.empty()));
    }

}
