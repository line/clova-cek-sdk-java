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

package com.linecorp.clova.extension.boot.message.payload;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Any type of an {@link Payload}.
 * <p>
 * Keeps all properties in {@link Map}.
 * But, this is just for internal process. For each extensions, converts appropriate the payload type.
 */
public class MapPayload extends LinkedHashMap<String, Object> implements Payload {

    private static final long serialVersionUID = 1L;

}
