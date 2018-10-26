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

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.matchers.Equality;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.linecorp.clova.extension.boot.handler.annnotation.CEKRequestHandler;
import com.linecorp.clova.extension.boot.handler.annnotation.IntentMapping;
import com.linecorp.clova.extension.boot.message.response.CEKResponse;
import com.linecorp.clova.extension.test.CEKRequestGenerator;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class HttpServletRequestResponseArgumentHandlerTest {

    @TestConfiguration
    static class TestConfig {

        @CEKRequestHandler
        static class TestHandler {

            @IntentMapping("RequestOnly")
            CEKResponse handleRequestOnly(HttpServletRequest request) {
                return CEKResponse.empty();
            }

            @IntentMapping("ResponseOnly")
            CEKResponse handleResponseOnly(HttpServletResponse response) {
                return CEKResponse.empty();
            }

            @IntentMapping("RequestAndResponse")
            CEKResponse handleRequestAndResponse(HttpServletRequest request, HttpServletResponse response) {
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

    @Before
    public void setUp() {
        reset(handler);
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void handleRequestOnly() throws Exception {
        MvcResult mvcResult = mvc.perform(post("/cek/v1")
                                                  .content(CEKRequestGenerator.requestBodyBuilder()
                                                                              .intent("RequestOnly")
                                                                              .build())
                                                  .contentType(MediaType.APPLICATION_JSON))
                                 .andDo(print())
                                 .andExpect(status().isOk())
                                 .andExpect(jsonPath("$.response.outputSpeech").doesNotExist())
                                 .andReturn();

        verify(handler).handleRequestOnly(equalsOrWrapper(mvcResult.getRequest()));
    }

    @Test
    public void handleResponseOnly() throws Exception {
        MvcResult mvcResult = mvc.perform(post("/cek/v1")
                                                  .content(CEKRequestGenerator.requestBodyBuilder()
                                                                              .intent("ResponseOnly")
                                                                              .build())
                                                  .contentType(MediaType.APPLICATION_JSON))
                                 .andDo(print())
                                 .andExpect(status().isOk())
                                 .andExpect(jsonPath("$.response.outputSpeech").doesNotExist())
                                 .andReturn();

        verify(handler).handleResponseOnly(equalsOrWrapper(mvcResult.getResponse()));
    }

    @Test
    public void handleRequestAndResponse() throws Exception {
        MvcResult mvcResult = mvc.perform(post("/cek/v1")
                                                  .content(CEKRequestGenerator.requestBodyBuilder()
                                                                              .intent("RequestAndResponse")
                                                                              .build())
                                                  .contentType(MediaType.APPLICATION_JSON))
                                 .andDo(print())
                                 .andExpect(status().isOk())
                                 .andExpect(jsonPath("$.response.outputSpeech").doesNotExist())
                                 .andReturn();

        verify(handler).handleRequestAndResponse(equalsOrWrapper(mvcResult.getRequest()),
                                                 equalsOrWrapper(mvcResult.getResponse()));
    }

    static HttpServletRequest equalsOrWrapper(HttpServletRequest request) {
        return argThat(new ArgumentMatcher<HttpServletRequest>() {
            @Override
            public boolean matches(HttpServletRequest argument) {
                HttpServletRequest original = getOriginalIfWrapped(argument);
                return Equality.areEqual(request, original);
            }

            private HttpServletRequest getOriginalIfWrapped(HttpServletRequest request) {
                if (request instanceof HttpServletRequestWrapper) {
                    return getOriginalIfWrapped(
                            (HttpServletRequest) ((HttpServletRequestWrapper) request).getRequest());
                }
                return request;
            }
        });
    }

    static HttpServletResponse equalsOrWrapper(HttpServletResponse response) {
        return argThat(new ArgumentMatcher<HttpServletResponse>() {
            @Override
            public boolean matches(HttpServletResponse argument) {
                HttpServletResponse original = getOriginalIfWrapped(argument);
                return Equality.areEqual(response, original);
            }

            private HttpServletResponse getOriginalIfWrapped(HttpServletResponse response) {
                if (response instanceof HttpServletResponseWrapper) {
                    return getOriginalIfWrapped(
                            (HttpServletResponse) ((HttpServletResponseWrapper) response).getResponse());
                }
                return response;
            }
        });
    }

}

