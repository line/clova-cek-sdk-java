/*
 * Copyright 2019 LINE Corporation
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
import static org.mockito.Mockito.reset;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.linecorp.clova.extension.boot.handler.annnotation.CEKRequestHandler;
import com.linecorp.clova.extension.boot.handler.annnotation.IntentMapping;
import com.linecorp.clova.extension.boot.message.request.DefaultSlotValueUnit;
import com.linecorp.clova.extension.boot.message.request.Slot;
import com.linecorp.clova.extension.boot.message.request.SlotValueUnit;
import com.linecorp.clova.extension.boot.message.response.CEKResponse;
import com.linecorp.clova.extension.test.CEKRequestGenerator;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("SlotUnitHandlerTest")
public class SlotUnitHandlerTest {

    @CEKRequestHandler
    @Profile("SlotUnitHandlerTest")
    static class TestHandler {

        @IntentMapping("SlotObject")
        CEKResponse handleSlotObject(Slot<String> slot) {
            return CEKResponse.empty();
        }

        @IntentMapping("SlotValueUnit")
        CEKResponse handleSlotValueUnit(SlotValueUnit<Double, Temperature.Unit> slot) {
            return CEKResponse.empty();
        }

        @IntentMapping("DefaultSlotValueUnit")
        CEKResponse handleDefaultSlotValueUnit(DefaultSlotValueUnit<Integer, Optional<Count.Unit>> slot) {
            return CEKResponse.empty();
        }

        @IntentMapping("CustomSlotValueUnit")
        CEKResponse handleCustomSlotValueUnit(Temperature slot) {
            return CEKResponse.empty();
        }

        @IntentMapping("OptionalSlotValueUnit")
        CEKResponse handleOptionalSlotValueUnit(Optional<SlotValueUnit<Integer, Count.Unit>> slot) {
            return CEKResponse.empty();
        }

        @IntentMapping("OptionalDefaultSlotValueUnit")
        CEKResponse handleOptionalDefaultSlotValueUnit(
                Optional<DefaultSlotValueUnit<Optional<Double>, Temperature.Unit>> slot) {
            return CEKResponse.empty();
        }

        @IntentMapping("OptionalCustomSlotValueUnit")
        CEKResponse handleOptionalCustomSlotValueUnit(Optional<Count> slot) {
            return CEKResponse.empty();
        }
    }

    @SpyBean
    TestHandler handler;

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper objectMapper;

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
    public void handleSlotObject_noUnit() throws Exception {
        ArgumentCaptor<Slot<String>> captor = argumentCaptor(Slot.class);
        doCallRealMethod().when(handler).handleSlotObject(captor.capture());

        String slotValue = RandomStringUtils.randomAlphabetic(10);

        mvc.perform(post("/cek/v1")
                            .content(CEKRequestGenerator.requestBodyBuilder()
                                                        .intent("SlotObject")
                                                        .slot("slot", slotValue)
                                                        .build())
                            .contentType(MediaType.APPLICATION_JSON))
           .andDo(print())
           .andExpect(status().isOk());

        assertThat(captor.getValue()).satisfies(actual -> {
            assertThat(actual.getName()).isEqualTo("slot");
            assertThat(actual.getValue()).isEqualTo(slotValue);
            assertThat(actual.getUnit()).isNull();
            assertThat(actual.getValueType()).isNull();
        });
    }

    @Test
    public void handleSlotObject_withUnit() throws Exception {
        ArgumentCaptor<Slot<String>> captor = argumentCaptor(Slot.class);
        doCallRealMethod().when(handler).handleSlotObject(captor.capture());

        String slotValue = RandomStringUtils.randomAlphabetic(10);
        String slotUnit = RandomStringUtils.randomAlphabetic(10);

        mvc.perform(post("/cek/v1")
                            .content(CEKRequestGenerator.requestBodyBuilder()
                                                        .intent("SlotObject")
                                                        .slot("slot", slotValue, slotUnit)
                                                        .build())
                            .contentType(MediaType.APPLICATION_JSON))
           .andDo(print())
           .andExpect(status().isOk());

        assertThat(captor.getValue()).satisfies(actual -> {
            assertThat(actual.getName()).isEqualTo("slot");
            assertThat(actual.getValue()).isEqualTo(slotValue);
            assertThat(actual.getUnit()).isEqualTo(slotUnit);
            assertThat(actual.getValueType()).isNull();
        });
    }

    @Test
    public void handleSlotValueUnit() throws Exception {
        ArgumentCaptor<SlotValueUnit<Double, Temperature.Unit>> captor = argumentCaptor(SlotValueUnit.class);
        doCallRealMethod().when(handler).handleSlotValueUnit(captor.capture());

        double slotValue = random.doubles(1, 10)
                                 .findFirst().getAsDouble();
        Temperature.Unit slotUnit = Temperature.Unit.values()[random.nextInt(Temperature.Unit.values().length)];

        mvc.perform(post("/cek/v1")
                            .content(CEKRequestGenerator.requestBodyBuilder()
                                                        .intent("SlotValueUnit")
                                                        .slot("slot", slotValue, slotUnit)
                                                        .build())
                            .contentType(MediaType.APPLICATION_JSON))
           .andDo(print())
           .andExpect(status().isOk());

        assertThat(captor.getValue()).isExactlyInstanceOf(DefaultSlotValueUnit.class)
                                     .satisfies(actual -> {
                                         assertThat(actual.getValue()).isEqualTo(slotValue);
                                         assertThat(actual.getUnit()).isEqualTo(slotUnit);
                                     });
    }

    @Test
    public void handleDefaultSlotValueUnit_withoutUnit() throws Exception {
        ArgumentCaptor<DefaultSlotValueUnit<Integer, Optional<Count.Unit>>> captor = argumentCaptor(
                Count.class);
        doCallRealMethod().when(handler).handleDefaultSlotValueUnit(captor.capture());

        int slotValue = random.ints(1, 10)
                              .findFirst().getAsInt();

        mvc.perform(post("/cek/v1")
                            .content(CEKRequestGenerator.requestBodyBuilder()
                                                        .intent("DefaultSlotValueUnit")
                                                        .slot("slot", slotValue)
                                                        .build())
                            .contentType(MediaType.APPLICATION_JSON))
           .andDo(print())
           .andExpect(status().isOk());

        assertThat(captor.getValue()).isExactlyInstanceOf(DefaultSlotValueUnit.class)
                                     .satisfies(actual -> {
                                         assertThat(actual.getValue()).isEqualTo(slotValue);
                                         assertThat(actual.getUnit()).isEmpty();
                                     });
    }

    @Test
    public void handleDefaultSlotValueUnit_withUnit() throws Exception {
        ArgumentCaptor<DefaultSlotValueUnit<Integer, Optional<Count.Unit>>> captor = argumentCaptor(
                Count.class);
        doCallRealMethod().when(handler).handleDefaultSlotValueUnit(captor.capture());

        int slotValue = random.ints(1, 10)
                              .findFirst().getAsInt();
        Count.Unit slotUnit = Count.Unit.values()[random.nextInt(Count.Unit.values().length)];

        mvc.perform(post("/cek/v1")
                            .content(CEKRequestGenerator.requestBodyBuilder()
                                                        .intent("DefaultSlotValueUnit")
                                                        .slot("slot", slotValue, slotUnit)
                                                        .build())
                            .contentType(MediaType.APPLICATION_JSON))
           .andDo(print())
           .andExpect(status().isOk());

        assertThat(captor.getValue()).isExactlyInstanceOf(DefaultSlotValueUnit.class)
                                     .satisfies(actual -> {
                                         assertThat(actual.getValue()).isEqualTo(slotValue);
                                         assertThat(actual.getUnit()).contains(slotUnit);
                                     });
    }

    @Test
    public void handleCustomSlotValueUnit() throws Exception {
        ArgumentCaptor<Temperature> captor = argumentCaptor(Temperature.class);
        doCallRealMethod().when(handler).handleCustomSlotValueUnit(captor.capture());

        double slotValue = random.doubles(1, 10)
                                 .findFirst().getAsDouble();
        Temperature.Unit slotUnit = Temperature.Unit.values()[random.nextInt(Temperature.Unit.values().length)];

        mvc.perform(post("/cek/v1")
                            .content(CEKRequestGenerator.requestBodyBuilder()
                                                        .intent("CustomSlotValueUnit")
                                                        .slot("slot", slotValue, slotUnit)
                                                        .build())
                            .contentType(MediaType.APPLICATION_JSON))
           .andDo(print())
           .andExpect(status().isOk());

        assertThat(captor.getValue()).isExactlyInstanceOf(Temperature.class)
                                     .satisfies(actual -> {
                                         assertThat(actual.getValue()).isEqualTo(slotValue);
                                         assertThat(actual.getUnit()).isEqualTo(slotUnit);
                                     });
    }

    // Optional

    @Test
    public void handleOptionalSlotValueUnit() throws Exception {
        ArgumentCaptor<Optional<SlotValueUnit<Integer, Count.Unit>>> captor = argumentCaptor(Optional.class);
        doCallRealMethod().when(handler).handleOptionalSlotValueUnit(captor.capture());

        int slotValue = random.ints(1, 10)
                              .findFirst().getAsInt();
        Count.Unit slotUnit = Count.Unit.values()[random.nextInt(Count.Unit.values().length)];

        mvc.perform(post("/cek/v1")
                            .content(CEKRequestGenerator.requestBodyBuilder()
                                                        .intent("OptionalSlotValueUnit")
                                                        .slot("slot", slotValue, slotUnit)
                                                        .build())
                            .contentType(MediaType.APPLICATION_JSON))
           .andDo(print())
           .andExpect(status().isOk());

        assertThat(captor.getValue()).hasValueSatisfying(actual -> {
            assertThat(actual).isExactlyInstanceOf(DefaultSlotValueUnit.class);
            assertThat(actual.getValue()).isEqualTo(slotValue);
            assertThat(actual.getUnit()).isEqualTo(slotUnit);
        });
    }

    @Test
    public void handleOptionalDefaultSlotValueUnit_withoutValue() throws Exception {
        ArgumentCaptor<Optional<DefaultSlotValueUnit<Optional<Double>, Temperature.Unit>>> captor =
                argumentCaptor(Optional.class);
        doCallRealMethod().when(handler).handleOptionalDefaultSlotValueUnit(captor.capture());

        Temperature.Unit slotUnit = Temperature.Unit.values()[random.nextInt(Temperature.Unit.values().length)];

        mvc.perform(post("/cek/v1")
                            .content(CEKRequestGenerator.requestBodyBuilder()
                                                        .intent("OptionalDefaultSlotValueUnit")
                                                        .slot("slot", "", slotUnit)
                                                        .build())
                            .contentType(MediaType.APPLICATION_JSON))
           .andDo(print())
           .andExpect(status().isOk());

        assertThat(captor.getValue()).hasValueSatisfying(actual -> {
            assertThat(actual).isExactlyInstanceOf(DefaultSlotValueUnit.class);
            assertThat(actual.getValue()).isEmpty();
            assertThat(actual.getUnit()).isEqualTo(slotUnit);
        });
    }

    @Test
    public void handleOptionalDefaultSlotValueUnit_withValue() throws Exception {
        ArgumentCaptor<Optional<DefaultSlotValueUnit<Optional<Double>, Temperature.Unit>>> captor =
                argumentCaptor(Optional.class);
        doCallRealMethod().when(handler).handleOptionalDefaultSlotValueUnit(captor.capture());

        double slotValue = random.doubles(1, 10)
                                 .findFirst().getAsDouble();
        Temperature.Unit slotUnit = Temperature.Unit.values()[random.nextInt(Temperature.Unit.values().length)];

        mvc.perform(post("/cek/v1")
                            .content(CEKRequestGenerator.requestBodyBuilder()
                                                        .intent("OptionalDefaultSlotValueUnit")
                                                        .slot("slot", slotValue, slotUnit)
                                                        .build())
                            .contentType(MediaType.APPLICATION_JSON))
           .andDo(print())
           .andExpect(status().isOk());

        assertThat(captor.getValue()).hasValueSatisfying(actual -> {
            assertThat(actual).isExactlyInstanceOf(DefaultSlotValueUnit.class);
            assertThat(actual.getValue()).contains(slotValue);
            assertThat(actual.getUnit()).isEqualTo(slotUnit);
        });
    }

    @Test
    public void handleOptionalCustomSlotValueUnit() throws Exception {
        ArgumentCaptor<Optional<Count>> captor = argumentCaptor(Optional.class);
        doCallRealMethod().when(handler).handleOptionalCustomSlotValueUnit(captor.capture());

        int slotValue = random.ints(1, 10)
                              .findFirst().getAsInt();
        Count.Unit slotUnit = Count.Unit.values()[random.nextInt(Count.Unit.values().length)];

        mvc.perform(post("/cek/v1")
                            .content(CEKRequestGenerator.requestBodyBuilder()
                                                        .intent("OptionalCustomSlotValueUnit")
                                                        .slot("slot", slotValue, slotUnit)
                                                        .build())
                            .contentType(MediaType.APPLICATION_JSON))
           .andDo(print())
           .andExpect(status().isOk());

        assertThat(captor.getValue()).hasValueSatisfying(actual -> {
            assertThat(actual).isExactlyInstanceOf(Count.class);
            assertThat(actual.getValue()).isEqualTo(slotValue);
            assertThat(actual.getUnit()).isEqualTo(slotUnit);
        });
    }

    @Data
    static class Temperature implements SlotValueUnit<Double, Temperature.Unit> {

        Double value;
        Unit unit;

        @RequiredArgsConstructor
        enum Unit {
            C("°C"),
            F("°F");

            @Getter(onMethod = @__(@JsonValue))
            private final String value;
        }
    }

    @Data
    static class Count implements SlotValueUnit<Integer, Count.Unit> {

        Integer value;
        Unit unit;

        @RequiredArgsConstructor
        enum Unit {
            KAI("回"),
            HON("本");

            @Getter(onMethod = @__(@JsonValue))
            private final String value;
        }
    }

}
