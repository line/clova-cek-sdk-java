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

import java.io.Serializable;

import com.linecorp.clova.extension.boot.message.directive.Directive;
import com.linecorp.clova.extension.boot.message.request.EventRequest;
import com.linecorp.clova.extension.boot.message.request.EventRequest.Event;

/**
 * An object that contains details for an {@link Event} or {@link Directive}
 * <p>
 * An {@link EventRequest} contains an Event. It has the device's event information, for example "AudioPlayer.Paused".
 * The Event object contains payload details, e.g. played music title, token, progress, etc.
 * Similarly, {@code CEKResponse} contains Directives and a Directive contains detailed payload information.
 */
public interface Payload extends Serializable {

    static Payload empty() {
        return new EmptyPayload();
    }

}
