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

package com.linecorp.clova.extension.boot.verifier;

import static com.linecorp.clova.extension.boot.verifier.CEKRequestSignatureVerifier.CLOVA_SIGNATURE_REQUEST_HEADER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;

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
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.StreamUtils;

import com.jayway.jsonpath.Configuration;

import com.linecorp.clova.extension.boot.controller.advice.CEKHandleIntentControllerAdvice;
import com.linecorp.clova.extension.boot.handler.annnotation.CEKRequestHandler;
import com.linecorp.clova.extension.boot.handler.annnotation.IntentMapping;
import com.linecorp.clova.extension.boot.message.response.CEKResponse;
import com.linecorp.clova.extension.test.CEKRequestGenerator;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "cek.verifier.signature.enabled=true")
public class CEKRequestSignatureVerifierTest {

    @TestConfiguration
    static class TestConfig {

        @CEKRequestHandler
        static class TestHandler {

            @IntentMapping("CEKRequestSignatureVerifierTest")
            CEKResponse handleSignatureVerificationTest() {
                return CEKResponse.empty();
            }
        }

    }

    @Autowired
    MockMvc mvc;

    @SpyBean
    TestConfig.TestHandler handler;

    @Autowired
    Configuration configuration;

    @SpyBean
    CEKHandleIntentControllerAdvice advice;

    @Captor
    ArgumentCaptor<SecurityException> captor;

    @Before
    public void setUp() {
        reset(handler, advice);
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void test_noHeader() throws Throwable {
        doCallRealMethod().when(advice).handle(captor.capture());

        mvc.perform(post("/cek/v1")
                            .content(CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                                        .intent("CEKRequestSignatureVerifierTest")
                                                        .build())
                            .contentType(MediaType.APPLICATION_JSON))
           .andDo(print())
           .andExpect(status().isBadRequest());

        verify(handler, never()).handleSignatureVerificationTest();

        assertThat(captor.getValue()).isExactlyInstanceOf(SecurityException.class)
                                     .hasMessage("SignatureCEK is missing.");
    }

    @Test
    public void test_hasSignatureHeader_wrongSignature() throws Throwable {
        doCallRealMethod().when(advice).handle(captor.capture());

        mvc.perform(post("/cek/v1")
                            .header(CLOVA_SIGNATURE_REQUEST_HEADER, RandomStringUtils.randomAlphabetic(10))
                            .content(CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                                        .intent("CEKRequestSignatureVerifierTest")
                                                        .build())
                            .contentType(MediaType.APPLICATION_JSON))
           .andDo(print())
           .andExpect(status().isBadRequest());

        verify(handler, never()).handleSignatureVerificationTest();

        assertThat(captor.getValue()).isExactlyInstanceOf(SecurityException.class)
                                     .hasMessage("Failed to verify the signature for the provided request.");
    }

    @Test
    public void test_hasSignatureHeader_correctSignature() throws Throwable {
        String signature = StreamUtils.copyToString(new ClassPathResource("data/signature").getInputStream(),
                                                    StandardCharsets.UTF_8).trim();

        mvc.perform(post("/cek/v1")
                            .header(CLOVA_SIGNATURE_REQUEST_HEADER, signature)
                            .content(CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                                        .intent("CEKRequestSignatureVerifierTest")
                                                        .build())
                            .contentType(MediaType.APPLICATION_JSON))
           .andDo(print())
           .andExpect(status().isOk());

        verify(handler).handleSignatureVerificationTest();
    }

}
