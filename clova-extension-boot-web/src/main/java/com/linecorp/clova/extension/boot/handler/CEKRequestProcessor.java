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

package com.linecorp.clova.extension.boot.handler;

import javax.servlet.http.HttpServletRequest;

import com.linecorp.clova.extension.boot.message.request.CEKRequestMessage;
import com.linecorp.clova.extension.boot.message.response.CEKResponseMessage;

import lombok.NonNull;

/**
 * An interface for processing a CEK request.
 */
public interface CEKRequestProcessor {

    /**
     * Processes the specified {@link CEKRequestMessage} and returns {@link CEKResponseMessage}.
     *
     * @param request        {@link HttpServletRequest}
     * @param requestMessage {@link CEKRequestMessage}
     * @return {@link CEKResponseMessage}
     * @throws Throwable Any throwables that occurred during processing.
     */
    CEKResponseMessage process(@NonNull HttpServletRequest request, @NonNull CEKRequestMessage requestMessage)
            throws Throwable;

}
