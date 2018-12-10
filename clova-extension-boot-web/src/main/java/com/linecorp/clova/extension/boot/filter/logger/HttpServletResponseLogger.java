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

import java.nio.charset.Charset;
import java.util.Optional;

import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;

/**
 * Logger for {@link javax.servlet.http.HttpServletResponse HttpServletResponse}.
 */
public interface HttpServletResponseLogger {

    /**
     * Writes out response body as string.
     *
     * @param response to write out
     */
    void log(ContentCachingResponseWrapper response);

    /**
     * Gets a body as string from given response.
     *
     * @param response to get body as text
     * @return response body as text
     */
    static String getBodyAsString(ContentCachingResponseWrapper response) {
        String encoding = Optional.ofNullable(response.getCharacterEncoding())
                                  .orElse(WebUtils.DEFAULT_CHARACTER_ENCODING);

        return new String(response.getContentAsByteArray(), Charset.forName(encoding));
    }

}
