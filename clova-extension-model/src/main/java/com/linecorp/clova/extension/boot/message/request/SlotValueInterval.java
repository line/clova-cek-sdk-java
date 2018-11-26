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
import java.time.temporal.TemporalAccessor;

import org.springframework.util.Assert;

import lombok.Data;

/**
 * Interval for {@link Slot} value.
 * <p>
 * If {@link Slot#valueType slot value type} is type of interval, the slot value type is this class. The values
 * contained in this class are provided by Date and Time API.
 * <p>
 * Whether the range is open-closed or closed is based on the specification of each Extension.
 *
 * @param <T> value types
 */
@Data
public class SlotValueInterval<T extends TemporalAccessor & Serializable & Comparable<? super T>>
        implements Serializable {

    private static final long serialVersionUID = 1L;

    private final T start;
    private final T end;

    /**
     * Constructs new instance with given start and end objects.
     *
     * @param start start of this interval
     * @param end   end of this interval
     */
    public SlotValueInterval(T start, T end) {
        Assert.notNull(start, "start should not be null.");
        Assert.notNull(end, "end should not be null.");
        Assert.isTrue(start.compareTo(end) < 0, "start should be smaller than end. " +
                                                "start:" + start + " end:" + end);
        this.start = start;
        this.end = end;
    }

}
