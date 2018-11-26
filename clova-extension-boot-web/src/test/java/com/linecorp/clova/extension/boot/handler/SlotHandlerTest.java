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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;

import com.linecorp.clova.extension.boot.handler.SlotHandlerTest.TestConfig.TestHandler;
import com.linecorp.clova.extension.boot.handler.annnotation.CEKRequestHandler;
import com.linecorp.clova.extension.boot.handler.annnotation.IntentMapping;
import com.linecorp.clova.extension.boot.handler.annnotation.SlotValue;
import com.linecorp.clova.extension.boot.message.request.Slot;
import com.linecorp.clova.extension.boot.message.request.SlotValueInterval;
import com.linecorp.clova.extension.boot.message.request.SlotValueType;
import com.linecorp.clova.extension.boot.message.response.CEKResponse;
import com.linecorp.clova.extension.test.CEKRequestGenerator;

import lombok.Data;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("SlotHandlerTest")
public class SlotHandlerTest {

    @TestConfiguration
    @Profile("SlotHandlerTest")
    static class TestConfig {
        @CEKRequestHandler
        @Profile("SlotHandlerTest")
        static class TestHandler {

            @IntentMapping("HasOptionalSlotValue")
            CEKResponse handleHasOptionalSlotValue(@SlotValue Optional<String> query) {
                return CEKResponse.empty();
            }

            @IntentMapping("HasNotRequiredSlotValue")
            CEKResponse handleHasNotRequiredSlotValue(@SlotValue(required = false) String query) {
                return CEKResponse.empty();
            }

            @IntentMapping("HasRequiredSlotValue")
            CEKResponse handleHasRequiredSlotValue(@SlotValue String query) {
                return CEKResponse.empty();
            }

            @IntentMapping("StringSlotValue")
            CEKResponse handleStringSlotValue(@SlotValue String slot) {
                return CEKResponse.empty();
            }

            @IntentMapping("OptionalStringSlotValue")
            CEKResponse handleOptionalStringSlotValue(@SlotValue Optional<String> slot) {
                return CEKResponse.empty();
            }

            @IntentMapping("StringSlot")
            CEKResponse handleStringSlot(Slot<String> slot) {
                return CEKResponse.empty();
            }

            @IntentMapping("OptionalStringSlot")
            CEKResponse handleOptionalStringSlot(Optional<Slot<String>> slot) {
                return CEKResponse.empty();
            }

            @IntentMapping("OptionalLocalDateIntervalSlot")
            CEKResponse handleOptionalLocalDateIntervalSlot(Optional<Slot<SlotValueInterval<LocalDate>>> slot) {
                return CEKResponse.empty();
            }

            @IntentMapping("OptionalLocalTimeIntervalSlotValue")
            CEKResponse handleOptionalLocalTimeIntervalSlotValue(
                    @SlotValue Optional<SlotValueInterval<LocalTime>> slot) {
                return CEKResponse.empty();
            }

            @IntentMapping("OffsetDateTimeIntervalSlot")
            CEKResponse handleOffsetDateTimeIntervalSlot(Slot<SlotValueInterval<OffsetDateTime>> slot) {
                return CEKResponse.empty();
            }

            @IntentMapping("LocalDateSlotValue")
            CEKResponse handleLocalDateSlotValue(@SlotValue LocalDate slot) {
                return CEKResponse.empty();
            }

            @IntentMapping("OptionalLocalTimeSlotValue")
            CEKResponse handleOptionalLocalTimeSlotValue(@SlotValue Optional<LocalTime> slot) {
                return CEKResponse.empty();
            }

            @IntentMapping("LocalDateSlot")
            CEKResponse handleLocalDateSlot(Slot<LocalDate> slot) {
                return CEKResponse.empty();
            }

            @IntentMapping("OptionalOffsetDateTimeSlot")
            CEKResponse handleOptionalOffsetDateTimeSlot(Optional<Slot<OffsetDateTime>> slot) {
                return CEKResponse.empty();
            }

            @IntentMapping("RandomDataSlotValue")
            CEKResponse handleRandomDataSlotValue(@SlotValue RandomData slot) {
                return CEKResponse.empty();
            }

            @IntentMapping("OptionalRandomDataSlotValue")
            CEKResponse handleOptionalRandomDataSlotValue(@SlotValue Optional<RandomData> slot) {
                return CEKResponse.empty();
            }

            @IntentMapping("RandomDataSlot")
            CEKResponse handleRandomDataSlot(Slot<RandomData> slot) {
                return CEKResponse.empty();
            }

            @IntentMapping("OptionalRandomDataSlot")
            CEKResponse handleOptionalRandomDataSlot(Optional<Slot<RandomData>> slot) {
                return CEKResponse.empty();
            }

            @IntentMapping("ObjectSlotValue")
            CEKResponse handleObjectSlotValue(@SlotValue Object slot) {
                return CEKResponse.empty();
            }

            @IntentMapping("OptionalObjectSlotValue")
            CEKResponse handleOptionalObjectSlotValue(@SlotValue Optional<Object> slot) {
                return CEKResponse.empty();
            }

            @IntentMapping("ObjectSlot")
            CEKResponse handleObjectSlot(Slot<Object> slot) {
                return CEKResponse.empty();
            }

            @IntentMapping("OptionalObjectSlot")
            CEKResponse handleOptionalObjectSlot(Optional<Slot<Object>> slot) {
                return CEKResponse.empty();
            }
        }
    }

    @SpyBean
    TestHandler handler;

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    @SuppressWarnings("SpringJavaAutowiringInspection")
    Configuration configuration;

    Random random = new Random();

    @After
    public void tearDown() {
        reset(handler);
    }

    @SuppressWarnings("unchecked")
    private static <T> ArgumentCaptor<T> argumentCaptor(Class<?> type) {
        return (ArgumentCaptor<T>) ArgumentCaptor.forClass(type);
    }

    @Test
    public void handleHasOptionalSlot_noSlot() throws Exception {
        mvc.perform(post("/cek/v1")
                            .content(CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                                        .intent("HasOptionalSlotValue")
                                                        .build())
                            .contentType(MediaType.APPLICATION_JSON))
           .andDo(print())
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.response.outputSpeech").doesNotExist());

        verify(handler).handleHasOptionalSlotValue(eq(Optional.empty()));
    }

    @Test
    public void handleHasOptionalSlot_hasSlot() throws Exception {
        String query = RandomStringUtils.randomAlphabetic(10);

        mvc.perform(post("/cek/v1")
                            .content(CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                                        .intent("HasOptionalSlotValue")
                                                        .slot("query", query)
                                                        .build())
                            .contentType(MediaType.APPLICATION_JSON))
           .andDo(print())
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.response.outputSpeech").doesNotExist());

        verify(handler).handleHasOptionalSlotValue(eq(Optional.of(query)));
    }

    @Test
    public void handleHasNotRequiredSlot_noSlot() throws Exception {
        mvc.perform(post("/cek/v1")
                            .content(CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                                        .intent("HasNotRequiredSlotValue")
                                                        .build())
                            .contentType(MediaType.APPLICATION_JSON))
           .andDo(print())
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.response.outputSpeech").doesNotExist());

        verify(handler).handleHasNotRequiredSlotValue(isNull());
    }

    @Test
    public void handleHasNotRequiredSlot_hasSlot() throws Exception {
        String query = RandomStringUtils.randomAlphabetic(10);

        mvc.perform(post("/cek/v1")
                            .content(CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                                        .intent("HasNotRequiredSlotValue")
                                                        .slot("query", query)
                                                        .build())
                            .contentType(MediaType.APPLICATION_JSON))
           .andDo(print())
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.response.outputSpeech").doesNotExist());

        verify(handler).handleHasNotRequiredSlotValue(eq(query));
    }

    @Test
    public void handleHasRequireSlot_noSlot() throws Exception {
        mvc.perform(post("/cek/v1")
                            .content(CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                                        .intent("HasRequiredSlotValue")
                                                        .build())
                            .contentType(MediaType.APPLICATION_JSON))
           .andDo(print())
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.response.outputSpeech.values.value").isNotEmpty());

        verify(handler, never()).handleHasRequiredSlotValue(any());
    }

    @Test
    public void handleHasRequiredSlot_hasSlot() throws Exception {
        String query = RandomStringUtils.randomAlphabetic(10);

        mvc.perform(post("/cek/v1")
                            .content(CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                                        .intent("HasRequiredSlotValue")
                                                        .slot("query", query)
                                                        .build())
                            .contentType(MediaType.APPLICATION_JSON))
           .andDo(print())
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.response.outputSpeech").doesNotExist());

        verify(handler).handleHasRequiredSlotValue(eq(query));
    }

    // handleStringSlotValue

    @Test
    public void handleStringSlotValue() throws Exception {
        String slot = RandomStringUtils.randomAlphabetic(10);
        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("StringSlotValue")
                                         .slot("slot", slot)
                                         .build();

        ArgumentCaptor<String> captor = argumentCaptor(String.class);
        doCallRealMethod().when(handler).handleStringSlotValue(captor.capture());

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON_UTF8))
           .andDo(print());

        assertThat(captor.getValue()).isEqualTo(slot);
    }

    @Test
    public void handleStringSlotValue_empty() throws Exception {
        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("StringSlotValue")
                                         .build();

        ArgumentCaptor<String> captor = argumentCaptor(String.class);
        doCallRealMethod().when(handler).handleStringSlotValue(captor.capture());

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON_UTF8))
           .andDo(print());

        verify(handler, never()).handleStringSlotValue(any());
    }

    @Test
    public void handleStringSlotValue_LocalDate() throws Exception {
        int days = random.nextInt(20) * (random.nextBoolean() ? 1 : -1);
        LocalDate slot = LocalDate.now().plusDays(days);
        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("StringSlotValue")
                                         .slot("slot", slot)
                                         .build();

        ArgumentCaptor<String> captor = argumentCaptor(String.class);
        doCallRealMethod().when(handler).handleStringSlotValue(captor.capture());

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON_UTF8))
           .andDo(print());

        assertThat(captor.getValue()).isEqualTo(SlotValueType.DATE.format(slot));
    }

    // handleOptionalStringSlotValue

    @Test
    public void handleOptionalStringSlotValue() throws Exception {
        String slot = RandomStringUtils.randomAlphabetic(10);
        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("OptionalStringSlotValue")
                                         .slot("slot", slot)
                                         .build();

        ArgumentCaptor<Optional<String>> captor = argumentCaptor(Optional.class);
        doCallRealMethod().when(handler).handleOptionalStringSlotValue(captor.capture());

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON_UTF8))
           .andDo(print());

        assertThat(captor.getValue()).contains(slot);
    }

    @Test
    public void handleOptionalStringSlotValue_empty() throws Exception {
        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("OptionalStringSlotValue")
                                         .build();

        ArgumentCaptor<Optional<String>> captor = argumentCaptor(Optional.class);
        doCallRealMethod().when(handler).handleOptionalStringSlotValue(captor.capture());

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON_UTF8))
           .andDo(print());

        assertThat(captor.getValue()).isEmpty();
    }

    @Test
    public void handleOptionalStringSlotValue_LocalDate() throws Exception {
        int days = random.nextInt(20) * (random.nextBoolean() ? 1 : -1);
        LocalDate slot = LocalDate.now().plusDays(days);
        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("OptionalStringSlotValue")
                                         .slot("slot", slot)
                                         .build();

        ArgumentCaptor<Optional<String>> captor = argumentCaptor(Optional.class);
        doCallRealMethod().when(handler).handleOptionalStringSlotValue(captor.capture());

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON_UTF8))
           .andDo(print());

        assertThat(captor.getValue()).contains(SlotValueType.DATE.format(slot));
    }

    // handleStringSlot

    @Test
    public void handleStringSlot() throws Exception {
        String slot = RandomStringUtils.randomAlphabetic(10);
        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("StringSlot")
                                         .slot("slot", slot)
                                         .build();

        ArgumentCaptor<Slot<String>> captor = argumentCaptor(Slot.class);
        doCallRealMethod().when(handler).handleStringSlot(captor.capture());

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON_UTF8))
           .andDo(print());

        assertThat(captor.getValue()).satisfies(actual -> {
            assertThat(actual.getName()).isEqualTo("slot");
            assertThat(actual.getValue()).isEqualTo(slot);
            assertThat(actual.getValueType()).isNull();
        });
    }

    @Test
    public void handleStringSlot_empty() throws Exception {
        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("StringSlot")
                                         .build();

        ArgumentCaptor<Slot<String>> captor = argumentCaptor(Slot.class);
        doCallRealMethod().when(handler).handleStringSlot(captor.capture());

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON_UTF8))
           .andDo(print());

        verify(handler, never()).handleStringSlot(any());
    }

    @Test
    public void handleStringSlot_LocalDate() throws Exception {
        int days = random.nextInt(20) * (random.nextBoolean() ? 1 : -1);
        LocalDate slot = LocalDate.now().plusDays(days);
        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("StringSlot")
                                         .slot("slot", slot)
                                         .build();

        ArgumentCaptor<Slot<String>> captor = argumentCaptor(Slot.class);
        doCallRealMethod().when(handler).handleStringSlot(captor.capture());

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON_UTF8))
           .andDo(print());

        assertThat(captor.getValue()).satisfies(actual -> {
            assertThat(actual.getName()).isEqualTo("slot");
            assertThat(actual.getValue()).isEqualTo(SlotValueType.DATE.format(slot));
            assertThat(actual.getValueType()).isEqualTo(SlotValueType.DATE);
        });
    }

    // handleOptionalStringSlot

    @Test
    public void handleOptionalStringSlot() throws Exception {
        String slot = RandomStringUtils.randomAlphabetic(10);
        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("OptionalStringSlot")
                                         .slot("Slot", slot)
                                         .build();

        ArgumentCaptor<Optional<Slot<String>>> captor = argumentCaptor(Optional.class);
        doCallRealMethod().when(handler).handleOptionalStringSlot(captor.capture());

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON_UTF8))
           .andDo(print());

        assertThat(captor.getValue()).hasValueSatisfying(actual -> {
            assertThat(actual.getName()).isEqualTo("Slot");
            assertThat(actual.getValue()).isEqualTo(slot);
            assertThat(actual.getValueType()).isNull();
        });
    }

    @Test
    public void handleOptionalStringSlot_empty() throws Exception {
        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("OptionalStringSlot")
                                         .build();

        ArgumentCaptor<Optional<Slot<String>>> captor = argumentCaptor(Optional.class);
        doCallRealMethod().when(handler).handleOptionalStringSlot(captor.capture());

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON_UTF8))
           .andDo(print());

        assertThat(captor.getValue()).isEmpty();
    }

    @Test
    public void handleOptionalStringSlot_LocalTime() throws Exception {
        int minutes = random.nextInt(20) * (random.nextBoolean() ? 1 : -1);
        LocalTime slot = LocalTime.now().plusMinutes(minutes).truncatedTo(ChronoUnit.SECONDS);
        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("OptionalStringSlot")
                                         .slot("slot", slot)
                                         .build();

        ArgumentCaptor<Optional<Slot<String>>> captor = argumentCaptor(Optional.class);
        doCallRealMethod().when(handler).handleOptionalStringSlot(captor.capture());

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON_UTF8))
           .andDo(print());

        assertThat(captor.getValue()).hasValueSatisfying(actual -> {
            assertThat(actual.getName()).isEqualTo("slot");
            assertThat(actual.getValue()).isEqualTo(SlotValueType.TIME.format(slot));
            assertThat(actual.getValueType()).isEqualTo(SlotValueType.TIME);
        });
    }

    // handleOptionalLocalDateIntervalSlot

    @Test
    public void handleOptionalLocalDateIntervalSlot() throws Exception {
        LocalDate start = LocalDate.now().minusDays(random.nextInt(20));
        LocalDate end = LocalDate.now().plusDays(random.nextInt(20));

        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("OptionalLocalDateIntervalSlot")
                                         .slot("slot", new SlotValueInterval<>(start, end))
                                         .build();

        ArgumentCaptor<Optional<Slot<SlotValueInterval<LocalDate>>>> captor = argumentCaptor(Optional.class);
        doCallRealMethod().when(handler).handleOptionalLocalDateIntervalSlot(captor.capture());

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON_UTF8))
           .andDo(print());

        assertThat(captor.getValue()).hasValueSatisfying(actual -> {
            assertThat(actual.getName()).isEqualTo("slot");
            assertThat(actual.getValue().getStart()).isEqualTo(start);
            assertThat(actual.getValue().getEnd()).isEqualTo(end);
            assertThat(actual.getValueType()).isEqualTo(SlotValueType.DATE_INTERVAL);
        });
    }

    // handleOptionalLocalTimeIntervalSlotValue

    @Test
    public void handleOptionalLocalTimeIntervalSlotValue() throws Exception {
        LocalTime start = LocalTime.now()
                                   .minusMinutes(random.nextInt(20))
                                   .truncatedTo(ChronoUnit.SECONDS);
        LocalTime end = LocalTime.now().plusMinutes(random.nextInt(20))
                                 .truncatedTo(ChronoUnit.SECONDS);

        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("OptionalLocalTimeIntervalSlotValue")
                                         .slot("slot", new SlotValueInterval<>(start, end))
                                         .build();

        ArgumentCaptor<Optional<SlotValueInterval<LocalTime>>> captor = argumentCaptor(Optional.class);
        doCallRealMethod().when(handler).handleOptionalLocalTimeIntervalSlotValue(captor.capture());

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON_UTF8))
           .andDo(print());

        assertThat(captor.getValue()).hasValueSatisfying(actual -> {
            assertThat(actual.getStart()).isEqualTo(start);
            assertThat(actual.getEnd()).isEqualTo(end);
        });
    }

    // handleOffsetDateTimeIntervalSlot

    @Test
    public void handleOffsetDateTimeIntervalSlot() throws Exception {
        OffsetDateTime start = OffsetDateTime.now()
                                             .minusDays(random.nextInt(10))
                                             .truncatedTo(ChronoUnit.SECONDS)
                                             .withOffsetSameInstant(ZoneOffset.ofHours(9));
        OffsetDateTime end = OffsetDateTime.now()
                                           .plusDays(random.nextInt(10))
                                           .truncatedTo(ChronoUnit.SECONDS)
                                           .withOffsetSameInstant(ZoneOffset.ofHours(9));

        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("OffsetDateTimeIntervalSlot")
                                         .slot("slot", new SlotValueInterval<>(start, end))
                                         .build();

        ArgumentCaptor<Slot<SlotValueInterval<OffsetDateTime>>> captor = argumentCaptor(Slot.class);
        doCallRealMethod().when(handler).handleOffsetDateTimeIntervalSlot(captor.capture());

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON_UTF8))
           .andDo(print());

        assertThat(captor.getValue()).satisfies(actual -> {
            assertThat(actual.getName()).isEqualTo("slot");
            assertThat(actual.getValue().getStart()).isEqualTo(start);
            assertThat(actual.getValue().getEnd()).isEqualTo(end);
            assertThat(actual.getValueType()).isEqualTo(SlotValueType.DATETIME_INTERVAL);
        });
    }

    // handleLocalDateSlotValue

    @Test
    public void handleLocalDateSlotValue() throws Exception {
        int days = random.nextInt(20) * (random.nextBoolean() ? 1 : -1);
        LocalDate slot = LocalDate.now().plusDays(days);
        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("LocalDateSlotValue")
                                         .slot("slot", slot)
                                         .build();

        ArgumentCaptor<LocalDate> captor = argumentCaptor(LocalDate.class);
        doCallRealMethod().when(handler).handleLocalDateSlotValue(captor.capture());

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON_UTF8))
           .andDo(print());

        assertThat(captor.getValue()).isEqualTo(slot);
    }

    @Test
    public void handleLocalDateSlotValue_empty() throws Exception {
        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("LocalDateSlotValue")
                                         .build();

        ArgumentCaptor<LocalDate> captor = argumentCaptor(LocalDate.class);
        doCallRealMethod().when(handler).handleLocalDateSlotValue(captor.capture());

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON_UTF8))
           .andDo(print());

        verify(handler, never()).handleLocalDateSlotValue(any());
    }

    // handleOptionalLocalTimeSlotValue

    @Test
    public void handleOptionalLocalTimeSlotValue() throws Exception {
        int minutes = random.nextInt(20) * (random.nextBoolean() ? 1 : -1);
        LocalTime slot = LocalTime.now().plusMinutes(minutes).truncatedTo(ChronoUnit.SECONDS);
        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("OptionalLocalTimeSlotValue")
                                         .slot("slot", slot)
                                         .build();

        ArgumentCaptor<Optional<LocalTime>> captor = argumentCaptor(Optional.class);
        doCallRealMethod().when(handler).handleOptionalLocalTimeSlotValue(captor.capture());

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON_UTF8))
           .andDo(print());

        assertThat(captor.getValue()).contains(slot);
    }

    @Test
    public void handleOptionalLocalTimeSlotValue_date() throws Exception {
        int days = random.nextInt(20) * (random.nextBoolean() ? 1 : -1);
        LocalDate slot = LocalDate.now().plusDays(days);
        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("OptionalLocalTimeSlotValue")
                                         .slot("slot", slot)
                                         .build();

        ArgumentCaptor<Optional<LocalTime>> captor = argumentCaptor(Optional.class);
        doCallRealMethod().when(handler).handleOptionalLocalTimeSlotValue(captor.capture());

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON_UTF8))
           .andDo(print());

        verify(handler, never()).handleOptionalLocalTimeSlotValue(any());
    }

    @Test
    public void handleOptionalLocalTimeSlotValue_empty() throws Exception {
        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("OptionalLocalTimeSlotValue")
                                         .build();

        ArgumentCaptor<Optional<LocalTime>> captor = argumentCaptor(Optional.class);
        doCallRealMethod().when(handler).handleOptionalLocalTimeSlotValue(captor.capture());

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON_UTF8))
           .andDo(print());

        assertThat(captor.getValue()).isEmpty();
    }

    // handleLocalDateSlot

    @Test
    public void handleLocalDateSlot() throws Exception {
        int days = random.nextInt(20) * (random.nextBoolean() ? 1 : -1);
        LocalDate slot = LocalDate.now().plusDays(days);
        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("LocalDateSlot")
                                         .slot("slot", slot)
                                         .build();

        ArgumentCaptor<Slot<LocalDate>> captor = argumentCaptor(Slot.class);
        doCallRealMethod().when(handler).handleLocalDateSlot(captor.capture());

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON_UTF8))
           .andDo(print());

        assertThat(captor.getValue()).satisfies(actual -> {
            assertThat(actual.getName()).isEqualTo("slot");
            assertThat(actual.getValue()).isEqualTo(slot);
            assertThat(actual.getValueType()).isEqualTo(SlotValueType.DATE);
        });
    }

    @Test
    public void handleLocalDateSlot_time() throws Exception {
        int minutes = random.nextInt(20) * (random.nextBoolean() ? 1 : -1);
        LocalTime slot = LocalTime.now().plusMinutes(minutes).truncatedTo(ChronoUnit.SECONDS);
        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("LocalDateSlot")
                                         .slot("slot", slot)
                                         .build();

        ArgumentCaptor<Slot<LocalDate>> captor = argumentCaptor(Slot.class);
        doCallRealMethod().when(handler).handleLocalDateSlot(captor.capture());

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON_UTF8))
           .andDo(print());

        verify(handler, never()).handleLocalDateSlot(any());
    }

    @Test
    public void handleLocalDateSlot_empty() throws Exception {
        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("LocalDateSlot")
                                         .build();

        ArgumentCaptor<Slot<LocalDate>> captor = argumentCaptor(Slot.class);
        doCallRealMethod().when(handler).handleLocalDateSlot(captor.capture());

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON_UTF8))
           .andDo(print());

        verify(handler, never()).handleLocalDateSlot(any());
    }

    // handleOptionalOffsetDateTimeSlot

    @Test
    public void handleOptionalOffsetDateTimeSlot() throws Exception {
        int days = random.nextInt(20) * (random.nextBoolean() ? 1 : -1);
        OffsetDateTime slot = OffsetDateTime.now().plusDays(days).truncatedTo(ChronoUnit.SECONDS)
                                            .withOffsetSameInstant(ZoneOffset.ofHours(9));
        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("OptionalOffsetDateTimeSlot")
                                         .slot("slot", slot)
                                         .build();

        ArgumentCaptor<Optional<Slot<OffsetDateTime>>> captor = argumentCaptor(Optional.class);
        doCallRealMethod().when(handler).handleOptionalOffsetDateTimeSlot(captor.capture());

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON_UTF8))
           .andDo(print());

        assertThat(captor.getValue()).hasValueSatisfying(actual -> {
            assertThat(actual.getName()).isEqualTo("slot");
            assertThat(actual.getValue()).isEqualTo(slot);
            assertThat(actual.getValueType()).isEqualTo(SlotValueType.DATETIME);
        });
    }

    @Test
    public void handleOptionalOffsetDateTimeSlot_empty() throws Exception {
        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("OptionalOffsetDateTimeSlot")
                                         .build();

        ArgumentCaptor<Optional<Slot<OffsetDateTime>>> captor = argumentCaptor(Optional.class);
        doCallRealMethod().when(handler).handleOptionalOffsetDateTimeSlot(captor.capture());

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON_UTF8))
           .andDo(print());

        assertThat(captor.getValue()).isEmpty();
    }

    //handleRandomDataSlotValue

    @Test
    public void handleRandomDataSlotValue() throws Exception {
        RandomData slot = new RandomData();
        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("RandomDataSlotValue")
                                         .slot("slot", slot)
                                         .build();

        ArgumentCaptor<RandomData> captor = argumentCaptor(RandomData.class);
        doCallRealMethod().when(handler).handleRandomDataSlotValue(captor.capture());

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON_UTF8))
           .andDo(print());

        assertThat(captor.getValue()).isEqualTo(slot);
    }

    @Test
    public void handleRandomDataSlotValue_empty() throws Exception {
        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("RandomDataSlotValue")
                                         .build();

        ArgumentCaptor<RandomData> captor = argumentCaptor(RandomData.class);
        doCallRealMethod().when(handler).handleRandomDataSlotValue(captor.capture());

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON_UTF8))
           .andDo(print());

        verify(handler, never()).handleRandomDataSlotValue(any());
    }

    // handleOptionalRandomDataSlotValue

    @Test
    public void handleOptionalRandomDataSlotValue() throws Exception {
        RandomData slot = new RandomData();
        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("OptionalRandomDataSlotValue")
                                         .slot("slot", slot)
                                         .build();

        ArgumentCaptor<Optional<RandomData>> captor = argumentCaptor(Optional.class);
        doCallRealMethod().when(handler).handleOptionalRandomDataSlotValue(captor.capture());

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON_UTF8))
           .andDo(print());

        assertThat(captor.getValue()).contains(slot);
    }

    @Test
    public void handleOptionalRandomDataSlotValue_empty() throws Exception {
        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("OptionalRandomDataSlotValue")
                                         .build();

        ArgumentCaptor<Optional<RandomData>> captor = argumentCaptor(Optional.class);
        doCallRealMethod().when(handler).handleOptionalRandomDataSlotValue(captor.capture());

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON_UTF8))
           .andDo(print());

        assertThat(captor.getValue()).isEmpty();
    }

    //handleRandomDataSlot

    @Test
    public void handleRandomDataSlot() throws Exception {
        RandomData slot = new RandomData();
        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("RandomDataSlot")
                                         .slot("slot", slot)
                                         .build();

        ArgumentCaptor<Slot<RandomData>> captor = argumentCaptor(Slot.class);
        doCallRealMethod().when(handler).handleRandomDataSlot(captor.capture());

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON_UTF8))
           .andDo(print());

        assertThat(captor.getValue()).satisfies(actual -> {
            assertThat(actual.getName()).isEqualTo("slot");
            assertThat(actual.getValue()).isEqualTo(slot);
            assertThat(actual.getValueType()).isNull();
        });
    }

    @Test
    public void handleRandomDataSlot_empty() throws Exception {
        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("RandomDataSlot")
                                         .build();

        ArgumentCaptor<Slot<RandomData>> captor = argumentCaptor(Slot.class);
        doCallRealMethod().when(handler).handleRandomDataSlot(captor.capture());

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON_UTF8))
           .andDo(print());

        verify(handler, never()).handleRandomDataSlot(any());
    }

    // handleOptionalRandomDataSlot

    @Test
    public void handleOptionalRandomDataSlot() throws Exception {
        RandomData slot = new RandomData();
        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("OptionalRandomDataSlot")
                                         .slot("slot", slot)
                                         .build();

        ArgumentCaptor<Optional<Slot<RandomData>>> captor = argumentCaptor(Optional.class);
        doCallRealMethod().when(handler).handleOptionalRandomDataSlot(captor.capture());

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON_UTF8))
           .andDo(print());

        assertThat(captor.getValue()).hasValueSatisfying(actual -> {
            assertThat(actual.getName()).isEqualTo("slot");
            assertThat(actual.getValue()).isEqualTo(slot);
            assertThat(actual.getValueType()).isNull();
        });
    }

    @Test
    public void handleOptionalRandomDataSlot_empty() throws Exception {
        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("OptionalRandomDataSlot")
                                         .build();

        ArgumentCaptor<Optional<Slot<RandomData>>> captor = argumentCaptor(Optional.class);
        doCallRealMethod().when(handler).handleOptionalRandomDataSlot(captor.capture());

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON_UTF8))
           .andDo(print());

        assertThat(captor.getValue()).isEmpty();
    }

    // handleObjectSlotValue

    @Test
    public void handleObjectSlotValue_empty() throws Exception {
        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("ObjectSlotValue")
                                         .build();

        ArgumentCaptor<Object> captor = argumentCaptor(Object.class);

        doCallRealMethod().when(handler).handleObjectSlotValue(captor.capture());

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON_UTF8))
           .andDo(print());

        verify(handler, never()).handleObjectSlotValue(any());
    }

    @Test
    public void handleObjectSlotValue_String() throws Exception {
        String slot = RandomStringUtils.randomAlphabetic(10);
        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("ObjectSlotValue")
                                         .slot("slot", slot)
                                         .build();

        ArgumentCaptor<Object> captor = argumentCaptor(Object.class);

        doCallRealMethod().when(handler).handleObjectSlotValue(captor.capture());

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON_UTF8))
           .andDo(print());

        assertThat(captor.getValue()).isEqualTo(slot);
    }

    @Test
    public void handleObjectSlotValue_RandomData() throws Exception {
        RandomData slot = new RandomData();
        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("ObjectSlotValue")
                                         .slot("slot", slot)
                                         .build();

        ArgumentCaptor<Object> captor = argumentCaptor(Object.class);

        doCallRealMethod().when(handler).handleObjectSlotValue(captor.capture());

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON_UTF8))
           .andDo(print());

        assertThat(captor.getValue()).isEqualTo(objectMapper.convertValue(slot, Map.class));
    }

    @Test
    public void handleObjectSlotValue_StringArray() throws Exception {
        String[] slot = new String[] {
                RandomStringUtils.randomNumeric(10), RandomStringUtils.randomNumeric(10)
        };
        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("ObjectSlotValue")
                                         .slot("slot", slot)
                                         .build();

        ArgumentCaptor<Object> captor = argumentCaptor(Object.class);

        doCallRealMethod().when(handler).handleObjectSlotValue(captor.capture());

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON_UTF8))
           .andDo(print());

        assertThat(captor.getValue()).isEqualTo(Arrays.asList(slot));
    }

    @Test
    public void handleObjectSlotValue_LocalDate() throws Exception {
        int days = random.nextInt(20) * (random.nextBoolean() ? 1 : -1);
        LocalDate slot = LocalDate.now().plusDays(days);
        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("ObjectSlotValue")
                                         .slot("slot", slot)
                                         .build();

        ArgumentCaptor<Object> captor = argumentCaptor(Object.class);

        doCallRealMethod().when(handler).handleObjectSlotValue(captor.capture());

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON_UTF8))
           .andDo(print());

        assertThat(captor.getValue()).isEqualTo(slot);
    }

    @Test
    public void handleObjectSlotValue_LocalTime() throws Exception {
        int minutes = random.nextInt(20) * (random.nextBoolean() ? 1 : -1);
        LocalTime slot = LocalTime.now().plusMinutes(minutes).truncatedTo(ChronoUnit.SECONDS);
        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("ObjectSlotValue")
                                         .slot("slot", slot)
                                         .build();

        ArgumentCaptor<Object> captor = argumentCaptor(Object.class);

        doCallRealMethod().when(handler).handleObjectSlotValue(captor.capture());

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON_UTF8))
           .andDo(print());

        assertThat(captor.getValue()).isEqualTo(slot);
    }

    @Test
    public void handleObjectSlotValue_OffsetDateTime() throws Exception {
        int days = random.nextInt(20) * (random.nextBoolean() ? 1 : -1);
        OffsetDateTime slot = OffsetDateTime.now().plusDays(days).truncatedTo(ChronoUnit.SECONDS)
                                            .withOffsetSameInstant(ZoneOffset.ofHours(9));
        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("ObjectSlotValue")
                                         .slot("slot", slot)
                                         .build();

        ArgumentCaptor<Object> captor = argumentCaptor(Object.class);

        doCallRealMethod().when(handler).handleObjectSlotValue(captor.capture());

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON_UTF8))
           .andDo(print());

        assertThat(captor.getValue()).isEqualTo(slot);
    }

    // handleOptionalObjectSlotValue

    @Test
    public void handleOptionalObjectSlotValue_empty() throws Exception {
        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("OptionalObjectSlotValue")
                                         .build();

        ArgumentCaptor<Optional<Object>> captor = argumentCaptor(Optional.class);

        doCallRealMethod().when(handler).handleOptionalObjectSlotValue(captor.capture());

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON_UTF8))
           .andDo(print());

        assertThat(captor.getValue()).isEmpty();
    }

    @Test
    public void handleOptionalObjectSlotValue_String() throws Exception {
        String slot = RandomStringUtils.randomAlphabetic(10);
        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("OptionalObjectSlotValue")
                                         .slot("slot", slot)
                                         .build();

        ArgumentCaptor<Optional<Object>> captor = argumentCaptor(Optional.class);

        doCallRealMethod().when(handler).handleOptionalObjectSlotValue(captor.capture());

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON_UTF8))
           .andDo(print());

        assertThat(captor.getValue()).contains(slot);
    }

    @Test
    public void handleOptionalObjectSlotValue_RandomData() throws Exception {
        RandomData slot = new RandomData();
        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("OptionalObjectSlotValue")
                                         .slot("slot", slot)
                                         .build();

        ArgumentCaptor<Optional<Object>> captor = argumentCaptor(Optional.class);

        doCallRealMethod().when(handler).handleOptionalObjectSlotValue(captor.capture());

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON_UTF8))
           .andDo(print());

        assertThat(captor.getValue()).contains(objectMapper.convertValue(slot, Map.class));
    }

    @Test
    public void handleOptionalObjectSlotValue_StringArray() throws Exception {
        String[] slot = new String[] {
                RandomStringUtils.randomNumeric(10), RandomStringUtils.randomNumeric(10)
        };
        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("OptionalObjectSlotValue")
                                         .slot("slot", slot)
                                         .build();

        ArgumentCaptor<Optional<Object>> captor = argumentCaptor(Optional.class);

        doCallRealMethod().when(handler).handleOptionalObjectSlotValue(captor.capture());

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON_UTF8))
           .andDo(print());

        assertThat(captor.getValue()).contains(Arrays.asList(slot));
    }

    @Test
    public void handleOptionalObjectSlotValue_LocalDate() throws Exception {
        int days = random.nextInt(20) * (random.nextBoolean() ? 1 : -1);
        LocalDate slot = LocalDate.now().plusDays(days);
        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("OptionalObjectSlotValue")
                                         .slot("slot", slot)
                                         .build();

        ArgumentCaptor<Optional<Object>> captor = argumentCaptor(Optional.class);

        doCallRealMethod().when(handler).handleOptionalObjectSlotValue(captor.capture());

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON_UTF8))
           .andDo(print());

        assertThat(captor.getValue()).contains(slot);
    }

    @Test
    public void handleOptionalObjectSlotValue_LocalTime() throws Exception {
        int minutes = random.nextInt(20) * (random.nextBoolean() ? 1 : -1);
        LocalTime slot = LocalTime.now().plusMinutes(minutes).truncatedTo(ChronoUnit.SECONDS);
        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("OptionalObjectSlotValue")
                                         .slot("slot", slot)
                                         .build();

        ArgumentCaptor<Optional<Object>> captor = argumentCaptor(Optional.class);

        doCallRealMethod().when(handler).handleOptionalObjectSlotValue(captor.capture());

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON_UTF8))
           .andDo(print());

        assertThat(captor.getValue()).contains(slot);
    }

    @Test
    public void handleOptionalObjectSlotValue_OffsetDateTime() throws Exception {
        int days = random.nextInt(20) * (random.nextBoolean() ? 1 : -1);
        OffsetDateTime slot = OffsetDateTime.now().plusDays(days).truncatedTo(ChronoUnit.SECONDS)
                                            .withOffsetSameInstant(ZoneOffset.ofHours(9));
        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("OptionalObjectSlotValue")
                                         .slot("slot", slot)
                                         .build();

        ArgumentCaptor<Optional<Object>> captor = argumentCaptor(Optional.class);

        doCallRealMethod().when(handler).handleOptionalObjectSlotValue(captor.capture());

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON_UTF8))
           .andDo(print());

        assertThat(captor.getValue()).contains(slot);
    }

    // handleObjectSlot

    @Test
    public void handleObjectSlot_empty() throws Exception {
        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("ObjectSlot")
                                         .build();

        ArgumentCaptor<Slot<Object>> captor = argumentCaptor(Slot.class);

        doCallRealMethod().when(handler).handleObjectSlot(captor.capture());

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON_UTF8))
           .andDo(print());

        verify(handler, never()).handleObjectSlot(any());
    }

    @Test
    public void handleObjectSlot_String() throws Exception {
        String slot = RandomStringUtils.randomAlphabetic(10);
        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("ObjectSlot")
                                         .slot("slot", slot)
                                         .build();

        ArgumentCaptor<Slot<Object>> captor = argumentCaptor(Slot.class);

        doCallRealMethod().when(handler).handleObjectSlot(captor.capture());

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON_UTF8))
           .andDo(print());

        assertThat(captor.getValue()).satisfies(actual -> {
            assertThat(actual.getName()).isEqualTo("slot");
            assertThat(actual.getValue()).isEqualTo(slot);
            assertThat(actual.getValueType()).isNull();
        });
    }

    @Test
    public void handleObjectSlot_RandomData() throws Exception {
        RandomData slot = new RandomData();
        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("ObjectSlot")
                                         .slot("slot", slot)
                                         .build();

        ArgumentCaptor<Slot<Object>> captor = argumentCaptor(Slot.class);

        doCallRealMethod().when(handler).handleObjectSlot(captor.capture());

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON_UTF8))
           .andDo(print());

        assertThat(captor.getValue()).satisfies(actual -> {
            assertThat(actual.getName()).isEqualTo("slot");
            assertThat(actual.getValue()).isEqualTo(objectMapper.convertValue(slot, Map.class));
            assertThat(actual.getValueType()).isNull();
        });
    }

    @Test
    public void handleObjectSlot_StringArray() throws Exception {
        String[] slot = new String[] {
                RandomStringUtils.randomNumeric(10), RandomStringUtils.randomNumeric(10)
        };
        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("ObjectSlot")
                                         .slot("slot", slot)
                                         .build();

        ArgumentCaptor<Slot<Object>> captor = argumentCaptor(Slot.class);

        doCallRealMethod().when(handler).handleObjectSlot(captor.capture());

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON_UTF8))
           .andDo(print());

        assertThat(captor.getValue()).satisfies(actual -> {
            assertThat(actual.getName()).isEqualTo("slot");
            assertThat(actual.getValue()).isEqualTo(Arrays.asList(slot));
            assertThat(actual.getValueType()).isNull();
        });
    }

    @Test
    public void handleObjectSlot_LocalDate() throws Exception {
        int days = random.nextInt(20) * (random.nextBoolean() ? 1 : -1);
        LocalDate slot = LocalDate.now().plusDays(days);
        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("ObjectSlot")
                                         .slot("slot", slot)
                                         .build();

        ArgumentCaptor<Slot<Object>> captor = argumentCaptor(Slot.class);

        doCallRealMethod().when(handler).handleObjectSlot(captor.capture());

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON_UTF8))
           .andDo(print());

        assertThat(captor.getValue()).satisfies(actual -> {
            assertThat(actual.getName()).isEqualTo("slot");
            assertThat(actual.getValue()).isEqualTo(slot);
            assertThat(actual.getValueType()).isEqualTo(SlotValueType.DATE);
        });
    }

    @Test
    public void handleObjectSlot_LocalTime() throws Exception {
        int minutes = random.nextInt(20) * (random.nextBoolean() ? 1 : -1);
        LocalTime slot = LocalTime.now().plusMinutes(minutes).truncatedTo(ChronoUnit.SECONDS);
        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("ObjectSlot")
                                         .slot("slot", slot)
                                         .build();

        ArgumentCaptor<Slot<Object>> captor = argumentCaptor(Slot.class);

        doCallRealMethod().when(handler).handleObjectSlot(captor.capture());

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON_UTF8))
           .andDo(print());

        assertThat(captor.getValue()).satisfies(actual -> {
            assertThat(actual.getName()).isEqualTo("slot");
            assertThat(actual.getValue()).isEqualTo(slot);
            assertThat(actual.getValueType()).isEqualTo(SlotValueType.TIME);
        });
    }

    @Test
    public void handleObjectSlot_OffsetDateTime() throws Exception {
        int days = random.nextInt(20) * (random.nextBoolean() ? 1 : -1);
        OffsetDateTime slot = OffsetDateTime.now().plusDays(days).truncatedTo(ChronoUnit.SECONDS)
                                            .withOffsetSameInstant(ZoneOffset.ofHours(9));
        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("ObjectSlot")
                                         .slot("slot", slot)
                                         .build();

        ArgumentCaptor<Slot<Object>> captor = argumentCaptor(Slot.class);

        doCallRealMethod().when(handler).handleObjectSlot(captor.capture());

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON_UTF8))
           .andDo(print());

        assertThat(captor.getValue()).satisfies(actual -> {
            assertThat(actual.getName()).isEqualTo("slot");
            assertThat(actual.getValue()).isEqualTo(slot);
            assertThat(actual.getValueType()).isEqualTo(SlotValueType.DATETIME);
        });
    }

    // handleOptionalObjectSlot

    @Test
    public void handleOptionalObjectSlot_empty() throws Exception {
        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("OptionalObjectSlot")
                                         .build();

        ArgumentCaptor<Optional<Slot<Object>>> captor = argumentCaptor(Optional.class);

        doCallRealMethod().when(handler).handleOptionalObjectSlot(captor.capture());

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON_UTF8))
           .andDo(print());

        assertThat(captor.getValue()).isEmpty();
    }

    @Test
    public void handleOptionalObjectSlot_String() throws Exception {
        String slot = RandomStringUtils.randomAlphabetic(10);
        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("OptionalObjectSlot")
                                         .slot("slot", slot)
                                         .build();

        ArgumentCaptor<Optional<Slot<Object>>> captor = argumentCaptor(Optional.class);

        doCallRealMethod().when(handler).handleOptionalObjectSlot(captor.capture());

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON_UTF8))
           .andDo(print());

        assertThat(captor.getValue()).hasValueSatisfying(actual -> {
            assertThat(actual.getName()).isEqualTo("slot");
            assertThat(actual.getValue()).isEqualTo(slot);
            assertThat(actual.getValueType()).isNull();
        });
    }

    @Test
    public void handleOptionalObjectSlot_RandomData() throws Exception {
        RandomData slot = new RandomData();
        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("OptionalObjectSlot")
                                         .slot("slot", slot)
                                         .build();

        ArgumentCaptor<Optional<Slot<Object>>> captor = argumentCaptor(Optional.class);

        doCallRealMethod().when(handler).handleOptionalObjectSlot(captor.capture());

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON_UTF8))
           .andDo(print());

        assertThat(captor.getValue()).hasValueSatisfying(actual -> {
            assertThat(actual.getName()).isEqualTo("slot");
            assertThat(actual.getValue()).isEqualTo(objectMapper.convertValue(slot, Map.class));
            assertThat(actual.getValueType()).isNull();
        });
    }

    @Test
    public void handleOptionalObjectSlot_StringArray() throws Exception {
        String[] slot = new String[] {
                RandomStringUtils.randomNumeric(10), RandomStringUtils.randomNumeric(10)
        };
        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("OptionalObjectSlot")
                                         .slot("slot", slot)
                                         .build();

        ArgumentCaptor<Optional<Slot<Object>>> captor = argumentCaptor(Optional.class);

        doCallRealMethod().when(handler).handleOptionalObjectSlot(captor.capture());

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON_UTF8))
           .andDo(print());

        assertThat(captor.getValue()).hasValueSatisfying(actual -> {
            assertThat(actual.getName()).isEqualTo("slot");
            assertThat(actual.getValue()).isEqualTo(Arrays.asList(slot));
            assertThat(actual.getValueType()).isNull();
        });
    }

    @Test
    public void handleOptionalObjectSlot_LocalDate() throws Exception {
        int days = random.nextInt(20) * (random.nextBoolean() ? 1 : -1);
        LocalDate slot = LocalDate.now().plusDays(days);
        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("OptionalObjectSlot")
                                         .slot("slot", slot)
                                         .build();

        ArgumentCaptor<Optional<Slot<Object>>> captor = argumentCaptor(Optional.class);

        doCallRealMethod().when(handler).handleOptionalObjectSlot(captor.capture());

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON_UTF8))
           .andDo(print());

        assertThat(captor.getValue()).hasValueSatisfying(actual -> {
            assertThat(actual.getName()).isEqualTo("slot");
            assertThat(actual.getValue()).isEqualTo(slot);
            assertThat(actual.getValueType()).isEqualTo(SlotValueType.DATE);
        });
    }

    @Test
    public void handleOptionalObjectSlot_LocalTime() throws Exception {
        int minutes = random.nextInt(20) * (random.nextBoolean() ? 1 : -1);
        LocalTime slot = LocalTime.now().plusMinutes(minutes).truncatedTo(ChronoUnit.SECONDS);
        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("OptionalObjectSlot")
                                         .slot("slot", slot)
                                         .build();

        ArgumentCaptor<Optional<Slot<Object>>> captor = argumentCaptor(Optional.class);

        doCallRealMethod().when(handler).handleOptionalObjectSlot(captor.capture());

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON_UTF8))
           .andDo(print());

        assertThat(captor.getValue()).hasValueSatisfying(actual -> {
            assertThat(actual.getName()).isEqualTo("slot");
            assertThat(actual.getValue()).isEqualTo(slot);
            assertThat(actual.getValueType()).isEqualTo(SlotValueType.TIME);
        });
    }

    @Test
    public void handleOptionalObjectSlot_OffsetDateTime() throws Exception {
        int days = random.nextInt(20) * (random.nextBoolean() ? 1 : -1);
        OffsetDateTime slot = OffsetDateTime.now().plusDays(days).truncatedTo(ChronoUnit.SECONDS)
                                            .withOffsetSameInstant(ZoneOffset.ofHours(9));
        String body = CEKRequestGenerator.requestBodyBuilder("data/request.json", configuration)
                                         .intent("OptionalObjectSlot")
                                         .slot("slot", slot)
                                         .build();

        ArgumentCaptor<Optional<Slot<Object>>> captor = argumentCaptor(Optional.class);

        doCallRealMethod().when(handler).handleOptionalObjectSlot(captor.capture());

        mvc.perform(post("/cek/v1")
                            .content(body)
                            .contentType(MediaType.APPLICATION_JSON_UTF8))
           .andDo(print());

        assertThat(captor.getValue()).hasValueSatisfying(actual -> {
            assertThat(actual.getName()).isEqualTo("slot");
            assertThat(actual.getValue()).isEqualTo(slot);
            assertThat(actual.getValueType()).isEqualTo(SlotValueType.DATETIME);
        });
    }

    @Data
    static class RandomData {

        String id = RandomStringUtils.randomAlphabetic(10);
        String name = RandomStringUtils.randomAlphabetic(10);
    }
}
