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

package com.linecorp.clova.extension.boot.session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.reset;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Repeat;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.jayway.jsonpath.Configuration;

import com.linecorp.clova.extension.boot.handler.annnotation.CEKRequestHandler;
import com.linecorp.clova.extension.boot.handler.annnotation.IntentMapping;
import com.linecorp.clova.extension.boot.message.request.CEKRequestMessage;
import com.linecorp.clova.extension.boot.message.response.CEKResponse;
import com.linecorp.clova.extension.test.CEKRequestGenerator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class SessionHolderTest {

    @TestConfiguration
    static class TestConfig {

        @CEKRequestHandler
        static class TestHandler {

            @IntentMapping("SessionHolder")
            CEKResponse handle(SessionHolder sessionHolder) {
                return CEKResponse.empty();
            }
        }

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    static class MyData {
        String name;
        String value;
    }

    @Autowired
    MockMvc mvc;

    @SpyBean
    TestConfig.TestHandler handler;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    Configuration configuration;

    @Before
    public void setUp() {
        reset(handler);
    }

    @Test
    public void handle_noSession() throws Exception {
        ArgumentCaptor<SessionHolder> captor = ArgumentCaptor.forClass(SessionHolder.class);
        doCallRealMethod().when(handler).handle(captor.capture());

        mvc.perform(post("/cek/v1")
                            .content(CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                                        .intent("SessionHolder")
                                                        .build())
                            .contentType(MediaType.APPLICATION_JSON))
           .andDo(print())
           .andExpect(jsonPath("$.response.shouldEndSession").value(true))
           .andExpect(jsonPath("$.sessionAttributes").isEmpty())
           .andExpect(status().isOk());

        assertThat(captor.getValue().getSessionAttributes()).isEmpty();
    }

    @Test
    @Repeat(5)
    public void handle_hasSession() throws Exception {
        MyData data1 = MyData.builder()
                             .name(RandomStringUtils.randomAlphabetic(10))
                             .value(RandomStringUtils.randomAlphabetic(10))
                             .build();
        MyData data2 = MyData.builder()
                             .name(RandomStringUtils.randomAlphabetic(10))
                             .value(RandomStringUtils.randomAlphabetic(10))
                             .build();

        ArgumentCaptor<SessionHolder> captor = ArgumentCaptor.forClass(SessionHolder.class);

        doAnswer(invocation -> {
            SessionHolder sessionHolder = invocation.getArgument(0);
            sessionHolder.setAttribute("data2", data2);
            return CEKResponse.builder()
                              .shouldEndSession(false)
                              .build();
        }).when(handler).handle(captor.capture());

        mvc.perform(post("/cek/v1")
                            .content(CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                                        .intent("SessionHolder")
                                                        .sessionAttribute("data1", data1)
                                                        .build())
                            .contentType(MediaType.APPLICATION_JSON))
           .andDo(print())
           .andExpect(jsonPath("$.response.shouldEndSession").value(false))
           .andExpect(jsonPath("$.sessionAttributes.data1.name").value(data1.getName()))
           .andExpect(jsonPath("$.sessionAttributes.data1.value").value(data1.getValue()))
           .andExpect(jsonPath("$.sessionAttributes.data2.name").value(data2.getName()))
           .andExpect(jsonPath("$.sessionAttributes.data2.value").value(data2.getValue()))
           .andExpect(status().isOk());

        SessionHolder actual = captor.getValue();

        assertThat(actual.getSession().getSessionAttributes())
                .containsKey("data1")
                .doesNotContainKey("data2");

        assertThat(actual.getAttribute("data1", MyData.class)).isEqualTo(data1);
    }

    @Test
    public void test_getAttribute_changeValue() {
        CEKRequestMessage.Session session = new CEKRequestMessage.Session();
        String sessionName = RandomStringUtils.randomAlphabetic(10);
        String sessionValue1 = RandomStringUtils.randomAlphabetic(10);
        String sessionValue2 = RandomStringUtils.randomAlphabetic(10);

        SessionHolder sessionHolder = new SessionHolder(objectMapper, session);
        sessionHolder.setAttribute(sessionName, sessionValue1);
        assertThat(sessionHolder.getAttribute(sessionName, String.class)).isEqualTo(sessionValue1);

        sessionHolder.setAttribute(sessionName, sessionValue2);

        assertThat(sessionHolder.getAttribute(sessionName, String.class)).isEqualTo(sessionValue2);

        sessionHolder.removeAttribute(sessionName);

        assertThat(sessionHolder.getAttribute(sessionName, String.class)).isNull();

        // null test
        assertThat(sessionHolder.getAttribute(RandomStringUtils.randomAlphabetic(10), String.class)).isNull();
    }

    @Test
    public void test_getAttribute_otherClass() {
        CEKRequestMessage.Session session = new CEKRequestMessage.Session();
        String sessionName = RandomStringUtils.randomAlphabetic(10);
        AImpl sessionValue = new AImpl(RandomStringUtils.randomAlphabetic(10));

        Map<String, Object> sessionAttributes = new HashMap<>();
        sessionAttributes.put(sessionName, sessionValue);
        session.setSessionAttributes(sessionAttributes);

        SessionHolder sessionHolder = new SessionHolder(objectMapper, session);

        assertThat(sessionHolder.getAttribute(sessionName, AImpl.class)).isEqualTo(sessionValue);
        assertThat(sessionHolder.getAttribute(sessionName, AImpl.class)).isEqualTo(sessionValue);

        assertThat(sessionHolder.getAttribute(sessionName, ABImpl.class)).satisfies(ab -> {
            assertThat(ab.getAValue()).isEqualTo(sessionValue.getAValue());
            assertThat(ab.getBValue()).isNull();
        });
    }

    @Test
    public void test_getAttribute_otherGenericClass() {
        CEKRequestMessage.Session session = new CEKRequestMessage.Session();
        String sessionName = RandomStringUtils.randomAlphabetic(10);
        List<AImpl> sessionValues = IntStream.range(0, 10)
                                             .mapToObj(n -> new AImpl(RandomStringUtils.randomAlphabetic(10)))
                                             .collect(Collectors.toList());

        Map<String, Object> sessionAttributes = new HashMap<>();
        sessionAttributes.put(sessionName, sessionValues);
        session.setSessionAttributes(sessionAttributes);

        SessionHolder sessionHolder = new SessionHolder(objectMapper, session);

        assertThat(sessionHolder.getAttribute(sessionName, new TypeReference<List<AImpl>>() {})).isEqualTo(
                sessionValues);
        assertThat(sessionHolder.getAttribute(sessionName, new TypeReference<List<AImpl>>() {})).isEqualTo(
                sessionValues);

        List<ABImpl> abList = sessionHolder.getAttribute(sessionName, new TypeReference<List<ABImpl>>() {});

        assertThat(abList).allSatisfy(ab -> {
            assertThat(ab.getBValue()).isNull();
        });

        assertThat(Lists.transform(abList, ABImpl::getAValue)).isEqualTo(
                Lists.transform(sessionValues, AImpl::getAValue));

        // null test
        assertThat(sessionHolder.getAttribute(RandomStringUtils.randomAlphabetic(10),
                                              new TypeReference<List<AImpl>>() {})).isNull();
    }

    interface AIface {
        String getAValue();
    }

    interface BIface {
        String getBValue();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class AImpl implements AIface {
        String aValue;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class ABImpl implements AIface, BIface {
        String aValue;
        String bValue;
    }

}
