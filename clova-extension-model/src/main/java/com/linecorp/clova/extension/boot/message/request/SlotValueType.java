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
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQuery;

import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The type of {@link Slot#value}.
 * <p>
 * If the slot value is resolved as date or time, the {@link Slot} has also a type of the value. This class has
 * all the types of it.
 */
@RequiredArgsConstructor
public enum SlotValueType {
    TIME("TIME", LocalTime.class, "HH:mm:ss"),
    DATE("DATE", LocalDate.class, "yyyy-MM-dd"),
    DATETIME("DATETIME", OffsetDateTime.class, "yyyy-MM-dd'T'HH:mm:ssXXX"),
    // Interval pattern
    TIME_INTERVAL("TIME.INTERVAL", SlotValueInterval.class, LocalTime.class, "HH:mm:ss/HH:mm:ss"),
    DATE_INTERVAL("DATE.INTERVAL", SlotValueInterval.class, LocalDate.class, "yyyy-MM-dd/yyyy-MM-dd"),
    DATETIME_INTERVAL("DATETIME.INTERVAL", SlotValueInterval.class, OffsetDateTime.class,
                      "yyyy-MM-dd'T'HH:mm:ssXXX/yyyy-MM-dd'T'HH:mm:ssXXX");

    @Getter(onMethod = @__(@JsonValue))
    private final String name;
    @Getter
    private final Class<?> valueType;
    @Getter
    private final Class<?> genericType;
    @Getter
    private final String pattern;

    private final SlotValueType singleType;

    SlotValueType(String name, Class<?> valueType, String pattern) {
        this.name = name;
        this.valueType = valueType;
        this.genericType = null;
        this.pattern = pattern;
        this.singleType = null;
    }

    SlotValueType(String name, Class<?> valueType, Class<?> genericType, String pattern) {
        this.name = name;
        this.valueType = valueType;
        this.genericType = genericType;
        this.pattern = pattern;
        this.singleType = singleInstanceTypeBy(genericType);
    }

    /**
     * Parse the given text as suitable type.
     *
     * @param text to be parsed
     * @param <T>  the suitable type ({@link #valueType})
     * @return parsed instance
     */
    @SuppressWarnings("unchecked")
    public <T> T parse(String text) {
        Assert.hasText(text, "text should not be blank.");
        if (SlotValueInterval.class.isAssignableFrom(this.valueType)) {
            String[] split = text.split("/");
            Assert.isTrue(split.length == 2, "text should contain '/'. text:" + text);
            TemporalAccessor start = this.singleType.parse(split[0]);
            TemporalAccessor end = this.singleType.parse(split[1]);
            return (T) new SlotValueInterval(start, end);
        }
        return (T) DateTimeFormatter.ofPattern(pattern).parse(text, temporalQueryBy(this.valueType));
    }

    /**
     * Format the given value as a text.
     *
     * @param temporalAccessor to be text
     * @param <T>              the given instance type
     * @return formatted text
     */
    public <T extends TemporalAccessor> String format(T temporalAccessor) {
        return DateTimeFormatter.ofPattern(pattern).format(temporalAccessor);
    }

    /**
     * Format the given value as a text.
     *
     * @param interval to be text
     * @param <T>      the given instance type
     * @return formatted text
     */
    public <T extends TemporalAccessor & Serializable & Comparable<? super T>> String format(
            SlotValueInterval<T> interval) {
        String start = this.singleType.format(interval.getStart());
        String end = this.singleType.format(interval.getEnd());
        return start + "/" + end;
    }

    private static SlotValueType singleInstanceTypeBy(Class<?> type) {
        if (type == LocalTime.class) {
            return TIME;
        }
        if (type == LocalDate.class) {
            return DATE;
        }
        if (type == OffsetDateTime.class) {
            return DATETIME;
        }
        throw new IllegalArgumentException("Unsupported type:" + type);
    }

    private static TemporalQuery<?> temporalQueryBy(Class<?> type) {
        if (type == LocalTime.class) {
            return LocalTime::from;
        }
        if (type == LocalDate.class) {
            return LocalDate::from;
        }
        if (type == OffsetDateTime.class) {
            return OffsetDateTime::from;
        }
        throw new IllegalArgumentException("Unsupported type:" + type);
    }

}
