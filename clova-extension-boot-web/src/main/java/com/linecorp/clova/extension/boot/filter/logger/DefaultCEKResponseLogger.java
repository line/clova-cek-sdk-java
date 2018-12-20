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

package com.linecorp.clova.extension.boot.filter.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.ContentCachingResponseWrapper;

import lombok.extern.slf4j.Slf4j;

/**
 * {@link HttpServletResponseLogger} for logging CEK Response in debug level.
 */
@Slf4j
public class DefaultCEKResponseLogger implements HttpServletResponseLogger {

    private static final Logger CEK_RESPONSE_MESSAGE_LOGGER =
            LoggerFactory.getLogger("cek.message.response");

    @Override
    public void log(ContentCachingResponseWrapper response) {
        if (!CEK_RESPONSE_MESSAGE_LOGGER.isDebugEnabled()) {
            return;
        }
        try {
            CEK_RESPONSE_MESSAGE_LOGGER.debug(
                    "CEK Payload <-- {}", HttpServletResponseLogger.getBodyAsString(response));
        } catch (Exception e) {
            log.warn("Failed to log response.", e);
        }
    }

}
