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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doCallRealMethod;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
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
import com.linecorp.clova.extension.boot.handler.annnotation.IntentMapping;
import com.linecorp.clova.extension.boot.handler.annnotation.SlotValue;
import com.linecorp.clova.extension.boot.message.response.CEKResponse;
import com.linecorp.clova.extension.test.CEKRequestGenerator;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class DateTimeSlotTest {

    @TestConfiguration
    static class TestConfig {

        @CEKRequestHandler
        static class TestHandler {

            @IntentMapping("DateSlot")
            CEKResponse handle(@SlotValue OffsetDateTime when) {
                return CEKResponse.empty();
            }

            @IntentMapping("DateSlot")
            CEKResponse handle(@SlotValue LocalDate when) {
                return CEKResponse.empty();
            }

            @IntentMapping("DateSlot")
            CEKResponse handle(@SlotValue LocalTime when) {
                return CEKResponse.empty();
            }

            @IntentMapping("DateSlot")
            CEKResponse handle(@SlotValue OffsetDateTime when1,
                               @SlotValue("when2") LocalTime time,
                               @SlotValue String place) {
                return CEKResponse.empty();
            }
        }

    }

    @Autowired
    MockMvc mvc;

    @SpyBean
    TestConfig.TestHandler handler;

    @Autowired
    @SuppressWarnings("SpringJavaAutowiringInspection")
    Configuration configuration;

    @Test
    public void handle_OffsetDateTime() throws Exception {
        ArgumentCaptor<OffsetDateTime> captor = ArgumentCaptor.forClass(OffsetDateTime.class);
        doCallRealMethod().when(handler).handle(captor.capture());

        OffsetDateTime slotValue = OffsetDateTime.now().withNano(0);

        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("DateSlot")
                                         .slotWithType("when", slotValue)
                                         .build();

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON))
           .andDo(print())
           .andExpect(status().isOk());

        assertThat(captor.getValue()).isEqualTo(slotValue);
    }

    @Test
    public void handle_LocalDate() throws Exception {
        ArgumentCaptor<LocalDate> captor = ArgumentCaptor.forClass(LocalDate.class);
        doCallRealMethod().when(handler).handle(captor.capture());

        LocalDate slotValue = LocalDate.now();

        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("DateSlot")
                                         .slotWithType("when", slotValue)
                                         .build();

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON))
           .andDo(print())
           .andExpect(status().isOk());

        assertThat(captor.getValue()).isEqualTo(slotValue);
    }

    @Test
    public void handle_LocalTime() throws Exception {
        ArgumentCaptor<LocalTime> captor = ArgumentCaptor.forClass(LocalTime.class);
        doCallRealMethod().when(handler).handle(captor.capture());

        LocalTime slotValue = LocalTime.now().withNano(0);

        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("DateSlot")
                                         .slotWithType("when", slotValue)
                                         .build();

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON))
           .andDo(print())
           .andExpect(status().isOk());

        assertThat(captor.getValue()).isEqualTo(slotValue);
    }

    @Test
    public void handle_Multi() throws Exception {
        ArgumentCaptor<OffsetDateTime> offsetDateTimeArgumentCaptor = ArgumentCaptor.forClass(
                OffsetDateTime.class);
        ArgumentCaptor<LocalTime> localTimeArgumentCaptor = ArgumentCaptor.forClass(LocalTime.class);
        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        doCallRealMethod().when(handler).handle(offsetDateTimeArgumentCaptor.capture(),
                                                localTimeArgumentCaptor.capture(),
                                                stringArgumentCaptor.capture());

        OffsetDateTime when1 = OffsetDateTime.now().withNano(0);
        LocalTime when2 = LocalTime.now().withNano(0);
        String place = UUID.randomUUID().toString();

        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("DateSlot")
                                         .slotWithType("when1", when1)
                                         .slotWithType("when2", when2)
                                         .slot("place", place)
                                         .build();

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON))
           .andDo(print())
           .andExpect(status().isOk());

        assertThat(offsetDateTimeArgumentCaptor.getValue()).isEqualTo(when1);
        assertThat(localTimeArgumentCaptor.getValue()).isEqualTo(when2);
        assertThat(stringArgumentCaptor.getValue()).isEqualTo(place);
    }

}
