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

package com.linecorp.clova.extension.boot.config;

import java.util.Locale;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * Configuration properties for Clova Extension Kit.
 */
@Data
@ConfigurationProperties("cek")
public class CEKProperties {

    /**
     * CEK API path.
     */
    private String apiPath = "/";

    /**
     * Client environment settings.
     */
    private Client client = new Client();

    /**
     * Clova client properties.
     */
    @Data
    public static class Client {

        /**
         * Clova client default locale.
         * <p>
         * Currently, there is no way for the extension to know the client locale.
         * Therefore, locale is always this value.
         */
        private Locale defaultLocale = Locale.JAPAN;

    }

}
