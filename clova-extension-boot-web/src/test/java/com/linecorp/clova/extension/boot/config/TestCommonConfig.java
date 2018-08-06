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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;

@Configuration
public class TestCommonConfig {

    @Bean
    com.jayway.jsonpath.Configuration jsonPathConfig(
            @SuppressWarnings("SpringJavaAutowiringInspection") ObjectMapper objectMapper) {
        return com.jayway.jsonpath.Configuration.builder()
                                                .mappingProvider(new JacksonMappingProvider(objectMapper))
                                                .jsonProvider(new JacksonJsonProvider(objectMapper))
                                                .build();
    }

}
