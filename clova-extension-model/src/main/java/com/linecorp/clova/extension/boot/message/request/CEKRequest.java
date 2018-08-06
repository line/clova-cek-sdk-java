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

import javax.validation.constraints.NotNull;

import lombok.Data;

/**
 * A base class which represents a request object included in {@link CEKRequestMessage} as an object.
 *
 * @see LaunchRequest
 * @see SessionEndedRequest
 * @see IntentRequest
 * @see EventRequest
 */
@Data
public abstract class CEKRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull
    private final RequestType type;

    protected CEKRequest(RequestType type) {
        this.type = type;
    }

    /**
     * Gets request name.
     *
     * <dl>
     * <dt>Launch, SessionEnded</dt>
     * <dd>blank</dd>
     * <dt>Intent</dt>
     * <dd>{@code {request.intent.name}}</dd>
     * <dt>Event</dt>
     * <dd>{@code {request.event.namespace}}.{@code {request.event.name}}</dd>
     * </dl>
     *
     * @return request name, not null, may be blank.
     */
    public abstract String getName();

}
