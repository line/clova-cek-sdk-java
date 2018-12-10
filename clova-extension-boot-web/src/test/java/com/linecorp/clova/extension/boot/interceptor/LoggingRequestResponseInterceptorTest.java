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

package com.linecorp.clova.extension.boot.interceptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.net.URI;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import com.linecorp.clova.extension.boot.interceptor.LoggingRequestResponseInterceptor.Config;

public class LoggingRequestResponseInterceptorTest {

    RestTemplate restTemplate = new RestTemplateBuilder()
            .interceptors(new LoggingRequestResponseInterceptor(Config.builder()
                                                                      .printResponseBody(false)
                                                                      .build()))
            .build();

    @Test
    public void test() {
        byte[] binary = RandomUtils.nextBytes(1024);
        String url = "http://localhost/media";
        MockRestServiceServer mockServer = MockRestServiceServer.bindTo(restTemplate).build();
        mockServer.expect(requestTo(url))
                  .andRespond(withSuccess(binary, MediaType.APPLICATION_OCTET_STREAM));

        byte[] actual = restTemplate.getForObject(URI.create(url), byte[].class);

        assertThat(actual).isEqualTo(binary);
    }

}
