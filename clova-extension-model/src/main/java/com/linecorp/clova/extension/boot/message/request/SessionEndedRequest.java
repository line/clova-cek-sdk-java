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

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Represents a {@code SessionEndRequest} as an object.
 * <p>
 * The SessionEndedRequest-type request is used to declare that the user has requested to stop using a specific mode
 * or custom extension.
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class SessionEndedRequest extends CEKRequest {

    private static final long serialVersionUID = 1L;

    @Override
    public String getName() {
        return "";
    }

    public SessionEndedRequest() {
        super(RequestType.SESSION_ENDED);
    }
}
