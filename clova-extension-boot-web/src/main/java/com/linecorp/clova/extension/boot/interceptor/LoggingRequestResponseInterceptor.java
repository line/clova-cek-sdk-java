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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.util.StreamUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link ClientHttpRequestInterceptor} for debug logging HTTP request and response.
 */
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class LoggingRequestResponseInterceptor implements ClientHttpRequestInterceptor {

    private Config config = Config.builder().build();

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        if (log.isDebugEnabled()) {
            return Execution.from(request, body, config)
                            .logRequest()
                            .execute(execution)
                            .logResponse()
                            .getResponse();
        }
        return execution.execute(request, body);
    }

    @Builder
    public static class Config {
        @Builder.Default
        private boolean printRequestBody = true;
        @Builder.Default
        private boolean printResponseBody = true;
        @Builder.Default
        @NonNull
        private ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json()
                                                                       .indentOutput(true)
                                                                       .build();
    }

    private static class Execution {
        private final String uuid = UUID.randomUUID().toString();

        private HttpRequest request;
        private byte[] body;

        private final Config config;

        @Getter
        private ClientHttpResponse response;

        Execution(HttpRequest request, byte[] body, Config config) {
            this.request = request;
            this.body = body;
            this.config = config;
        }

        static Execution from(HttpRequest request, byte[] body, Config config) {
            return new Execution(request, body, config);
        }

        Execution execute(ClientHttpRequestExecution execution) throws IOException {
            long start = System.currentTimeMillis();
            this.response = execution.execute(this.request, this.body);
            log.debug("Time:[uuid:[{}]: time:[{}msec], uri:[{}]]",
                      this.uuid,
                      System.currentTimeMillis() - start,
                      this.request.getURI());
            return this;
        }

        Execution logRequest() {
            final String requestBody = this.body != null && this.body.length > 0
                                       ? new String(this.body, extractCharset(this.request.getHeaders()))
                                       : null;

            if (config.printRequestBody) {
                log.debug("Request:[uuid:[{}], method:[{}], uri:[{}], headers:[{}]], body:[\n{}\n]",
                          this.uuid,
                          this.request.getMethod(),
                          this.request.getURI(),
                          this.request.getHeaders(),
                          requestBody);
            } else {
                log.debug("Request:[uuid:[{}], method:[{}], uri:[{}], headers:[{}]]",
                          this.uuid,
                          this.request.getMethod(),
                          this.request.getURI(),
                          this.request.getHeaders());
            }

            return this;
        }

        Execution logResponse() {
            SafeResponse safe = new SafeResponse(this.response);

            if (config.printResponseBody) {
                String responseBodyAsText = safe.getResponseBodyAsText();
                MediaType contentType = safe.getHttpHeaders().getContentType();
                if (contentType != null && contentType.isCompatibleWith(MediaType.APPLICATION_JSON)) {
                    try {
                        // For indent output
                        responseBodyAsText = config.objectMapper.writeValueAsString(
                                config.objectMapper.readValue(responseBodyAsText,
                                                              new TypeReference<Map<String, Object>>() {}));
                    } catch (Exception e) {
                        // nop
                    }
                }

                log.debug("Response:[uuid:[{}], status:[{}], text:[{}], headers:[{}]], body:[\n{}\n]",
                          this.uuid, safe.getStatusCode(), safe.getStatusText(), safe.getHttpHeaders(),
                          responseBodyAsText);
            } else {
                log.debug("Response:[uuid:[{}], status:[{}], text:[{}], headers:[{}]]",
                          this.uuid, safe.getStatusCode(), safe.getStatusText(), safe.getHttpHeaders());
            }

            this.response = safe.createRecycledClientHttpResponse();

            return this;
        }
    }

    private static Charset extractCharset(HttpHeaders httpHeaders) {
        return Optional.ofNullable(httpHeaders.getContentType())
                       .map(MediaType::getCharset)
                       .orElse(StandardCharsets.UTF_8);
    }

    @Getter
    private static class SafeResponse {

        private final ClientHttpResponse original;

        private final Charset responseCharset;

        private final String responseBodyAsText;
        private final String statusText;
        private final HttpStatus statusCode;
        private final HttpHeaders httpHeaders;

        SafeResponse(ClientHttpResponse response) {
            this.original = response;
            this.responseCharset = extractCharset(response.getHeaders());

            this.responseBodyAsText = ignoreError(() -> {
                try (InputStream body = response.getBody()) {
                    return StreamUtils.copyToString(body, this.responseCharset);
                }
            });
            this.statusText = ignoreError(response::getStatusText);
            this.statusCode = ignoreError(response::getStatusCode);
            this.httpHeaders = ignoreError(response::getHeaders);
        }

        ClientHttpResponse createRecycledClientHttpResponse() {
            return new RecycledClientHttpResponse(this.original, this.responseBodyAsText, this.responseCharset);
        }

        private static <T> T ignoreError(Callable<T> callable) {
            try {
                return callable.call();
            } catch (Throwable t) {
                return null;
            }
        }
    }

    @RequiredArgsConstructor
    private static class RecycledClientHttpResponse implements ClientHttpResponse {

        private final ClientHttpResponse original;
        private final String responseBodyAsText;
        private final Charset responseCharset;

        @Override
        public HttpHeaders getHeaders() {
            return this.original.getHeaders();
        }

        @Override
        public InputStream getBody() throws IOException {
            return new ByteArrayInputStream(this.responseBodyAsText.getBytes(this.responseCharset));
        }

        @Override
        public HttpStatus getStatusCode() throws IOException {
            return this.original.getStatusCode();
        }

        @Override
        public int getRawStatusCode() throws IOException {
            return this.original.getRawStatusCode();
        }

        @Override
        public String getStatusText() throws IOException {
            return this.original.getStatusText();
        }

        @Override
        public void close() {
            this.original.close();
        }
    }
}
