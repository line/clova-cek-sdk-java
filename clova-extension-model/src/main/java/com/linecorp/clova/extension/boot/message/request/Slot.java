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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.validation.constraints.NotBlank;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Slots are basic variables in {@link IntentRequest CEK Intent request}.
 *
 * @param <T> slot value type
 */
@Data
@NoArgsConstructor
public class Slot<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Map<Class<?>, Method> PARSE_METHOD_CACHE = new ConcurrentHashMap<>();

    @NotBlank
    private String name;
    private T value;
    private String unit;
    private SlotValueType valueType;

    /**
     * Creates new instance with the given value.
     *
     * @param value slot value of new instance
     * @param <R>   new slot value type
     * @return new Slot instance
     */
    public <R> Slot<R> withNewValue(R value) {
        Slot<R> slot = new Slot<>();
        slot.setName(this.name);
        slot.setValue(value);
        slot.setUnit(this.unit);
        slot.setValueType(this.valueType);
        return slot;
    }

}
