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

package com.linecorp.clova.extension.boot.message.response;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Data;

/**
 * Represents a Clova custom extension response message as an object.
 *
 * <p>The extension should return a specifically formatted JSON object.
 * CEKResponseMessage represents a valid response
 * JSON structure as an object.
 *
 * <p>This object includes a {@link CEKResponse}, session information, and CEK message format version.
 */
@Data
public class CEKResponseMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull
    @Valid
    private final CEKResponse response;
    @NotNull
    private Map<String, Object> sessionAttributes = Collections.emptyMap();
    @NotBlank
    private String version = "1";

}
