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

package com.linecorp.clova.extension.boot.message.request;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Represents an {@code IntentRequest} as an object.
 * <p>
 * The IntentRequest-type request is used by the CEK to send user requests to the extension based on the predefined
 * interaction model.
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class IntentRequest extends CEKRequest {

    private static final long serialVersionUID = 1L;

    @NotNull
    @Valid
    private Intent intent;

    public IntentRequest() {
        super(RequestType.INTENT);
    }

    @Override
    public String getName() {
        return intent.getName();
    }

    @Data
    public static class Intent implements Serializable {

        private static final long serialVersionUID = 1L;

        @NotBlank
        private String name;
        @NotNull
        @Valid
        private Map<String, Slot> slots;

        @Data
        @NoArgsConstructor
        public static class Slot implements Serializable {

            private static final long serialVersionUID = 1L;

            private static final Map<Class<?>, Method> PARSE_METHOD_CACHE = new ConcurrentHashMap<>();

            @NotBlank
            private String name;
            private Object value;
            private SlotValueType valueType;

            public Object convertValueAsType() {
                if (this.valueType != null && this.valueType.getSlotValueType() != null
                    && this.value instanceof String) {
                    return parse();
                }
                return null;
            }

            private Object parse() {
                if (this.valueType.getSlotValueType() == LocalDate.class) {
                    return LocalDate.parse((String) this.value,
                                           DateTimeFormatter.ofPattern(this.valueType.getFormat()));
                }
                if (this.valueType.getSlotValueType() == LocalTime.class) {
                    return LocalTime.parse((String) this.value,
                                           DateTimeFormatter.ofPattern(this.valueType.getFormat()));
                }
                if (this.valueType.getSlotValueType() == OffsetDateTime.class) {
                    return OffsetDateTime.parse((String) this.value,
                                                DateTimeFormatter.ofPattern(this.valueType.getFormat()));
                }
                throw new IllegalArgumentException(
                        String.format("Unsupported value type. [%s]", this.valueType));
            }

            @RequiredArgsConstructor
            public enum SlotValueType {
                DATE("DATE", LocalDate.class, "yyyy-MM-dd"),
                TIME("TIME", LocalTime.class, "HH:mm:ss"),
                DATETIME("DATETIME", OffsetDateTime.class, "yyyy-MM-dd'T'HH:mm:ssXXX"),
                // Now, not supported
                DATETIME_INTERVAL("DATETIME.INTERVAL", null, null),
                TIME_INTERVAL("TIME.INTERVAL", null, null),
                DATE_INTERVAL("DATE.INTERVAL", null, null);

                @Getter
                private final String value;
                @Getter
                private final Class<?> slotValueType;
                @Getter
                private final String format;

                @JsonCreator
                public static SlotValueType identifyByValue(String value) {
                    if (value == null || value.isEmpty()) {
                        return null;
                    }
                    return Arrays.stream(values())
                                 .filter(slotValue -> slotValue.value.equals(value))
                                 .findFirst()
                                 .orElse(null);
                }
            }
        }
    }
}
