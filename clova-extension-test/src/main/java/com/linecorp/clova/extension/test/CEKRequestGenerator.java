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

package com.linecorp.clova.extension.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.PropertyNamingStrategy.SnakeCaseStrategy;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;

import com.linecorp.clova.extension.boot.message.context.ContextProperty;
import com.linecorp.clova.extension.boot.message.request.RequestType;
import com.linecorp.clova.extension.boot.message.request.SlotValueInterval;
import com.linecorp.clova.extension.boot.message.request.SlotValueType;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CEK request generator for tests.
 */
public class CEKRequestGenerator {

    private static final String DEFAULT_TEMPLATE_PATH = "test-data/templates";
    private static final String DEFAULT_REQUEST_TEMPLATE_PATH = DEFAULT_TEMPLATE_PATH + "/cek-request.json";
    private static final ObjectMapper DEFAULT_MAPPER = Jackson2ObjectMapperBuilder.json().build();

    public static RequestBodyBuilder requestBodyBuilder() {
        return new RequestBodyBuilder();
    }

    /**
     * Constructs {@link RequestBodyBuilder} with JSON template path.
     *
     * @param path JSON template path (placed in classpath)
     * @return {@link RequestBodyBuilder}
     */
    public static RequestBodyBuilder requestBodyBuilder(String path) {
        return new RequestBodyBuilder()
                .resource(path);
    }

    /**
     * Constructs {@link RequestBodyBuilder} with JSON template path.
     *
     * @param path          JSON template path (placed in classpath)
     * @param configuration configuration for JSON serialization
     * @return {@link RequestBodyBuilder}
     * @deprecated Use {@link #requestBodyBuilder(String, ObjectMapper)} instead of this.
     */
    @Deprecated
    public static RequestBodyBuilder requestBodyBuilder(String path, Configuration configuration) {
        return new RequestBodyBuilder()
                .resource(path)
                .configuration(configuration);
    }

    /**
     * Constructs {@link RequestBodyBuilder} with JSON template path.
     *
     * @param objectMapper For JSON serialization
     * @return {@link RequestBodyBuilder}
     */
    public static RequestBodyBuilder requestBodyBuilder(ObjectMapper objectMapper) {
        return new RequestBodyBuilder()
                .objectMapper(objectMapper);
    }

    /**
     * Constructs {@link RequestBodyBuilder} with JSON template path.
     *
     * @param path         JSON template path (placed in classpath)
     * @param objectMapper For JSON serialization
     * @return {@link RequestBodyBuilder}
     */
    public static RequestBodyBuilder requestBodyBuilder(String path, ObjectMapper objectMapper) {
        return new RequestBodyBuilder()
                .resource(path)
                .objectMapper(objectMapper);
    }

    public static <P extends ContextProperty> P createContext(Class<P> contextType) {
        return createContext(contextType, DEFAULT_MAPPER);
    }

    public static <P extends ContextProperty> P createContext(Class<P> contextType, ObjectMapper objectMapper) {
        String fileName = ((SnakeCaseStrategy) PropertyNamingStrategy.SNAKE_CASE)
                .translate(contextType.getSimpleName());
        if (fileName.endsWith("_context")) {
            fileName = fileName.substring(0, fileName.length() - "_context".length());
        }

        String filePath = DEFAULT_TEMPLATE_PATH + "/" + fileName + ".json";
        try {
            return objectMapper.readValue(new ClassPathResource(filePath).getInputStream(), contextType);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Builder of CEK request.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class RequestBodyBuilder {
        private String path = DEFAULT_REQUEST_TEMPLATE_PATH;
        private ObjectMapper objectMapper = DEFAULT_MAPPER;

        private Charset resourceCharset = StandardCharsets.UTF_8;
        private Map<String, Object> placeholder = new HashMap<>();
        private Map<String, List<Object>> additionalArrays = new HashMap<>();
        private Map<String, Object> additionalObjects = new HashMap<>();
        private Collection<String> removePaths = new HashSet<>();

        private Configuration configuration;

        /**
         * Sets JSON template path
         *
         * @param path JSON template path (placed in classpath)
         * @return this instance
         */
        RequestBodyBuilder resource(String path) {
            this.path = path;
            return this;
        }

        /**
         * Sets {@link ObjectMapper}.
         *
         * @param objectMapper {@link ObjectMapper}
         * @return this instance
         */
        RequestBodyBuilder objectMapper(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
            return this;
        }

        /**
         * Sets encoding of template file.
         * <p>
         * Not required. In default, encoding is UTF-8.
         *
         * @param charset encoding of template file.
         * @return this instance
         */
        public RequestBodyBuilder resourceCharset(Charset charset) {
            this.resourceCharset = charset;
            return this;
        }

        public RequestBodyBuilder context(String name, ContextProperty context) {
            return put("$.context." + name, context);
        }

        /**
         * Sets request type.
         * <p>
         * Not required. In default, request type is {@link RequestType#INTENT INTENT}.
         *
         * @param requestType request type
         * @return this instance
         */
        private RequestBodyBuilder requestType(RequestType requestType) {
            return put("$.request.type", requestType.getValue());
        }

        /**
         * Sets {@link RequestType#LAUNCH LAUNCH} to request type.
         *
         * @return this instance
         */
        public RequestBodyBuilder launch() {
            return requestType(RequestType.LAUNCH);
        }

        /**
         * Sets {@link RequestType#SESSION_ENDED SESSION_ENDED} to request type.
         *
         * @return this instance
         */
        public RequestBodyBuilder sessionEnded() {
            return requestType(RequestType.SESSION_ENDED);
        }

        /**
         * Sets {@link RequestType#INTENT INTENT} to request type, and sets the specified to intent name.
         *
         * @param intentName intent name
         * @return this instance
         */
        public RequestBodyBuilder intent(String intentName) {
            return requestType(RequestType.INTENT)
                    .put("$.request.intent.name", intentName);
        }

        /**
         * Sets {@link RequestType#EVENT EVENT} to request type, and sets the specified to event namespace and
         * name.
         *
         * @param event event namespace and name joined by dot. e.g.) Demo.Start
         * @return this instance
         */
        public RequestBodyBuilder event(String event) {
            String[] eventNamespaceAndName = event.split("\\.");
            return requestType(RequestType.EVENT)
                    .put("$.request.event.namespace", eventNamespaceAndName[0])
                    .put("$.request.event.name", eventNamespaceAndName[1]);
        }

        public RequestBodyBuilder placeholder(String key, Object value) {
            this.placeholder.put(key, value);
            return this;
        }

        public RequestBodyBuilder sessionAttribute(String name, Object value) {
            this.additionalObjects.put("$.session.sessionAttributes." + name, value);
            return this;
        }

        public RequestBodyBuilder slot(String name, Object value) {
            this.additionalObjects.put("$.request.intent.slots." + name, new Slot(name, value));
            return this;
        }

        public RequestBodyBuilder slot(String name, Object value, Object unit) {
            this.additionalObjects.put("$.request.intent.slots." + name, new Slot(name, value, unit));
            return this;
        }

        public RequestBodyBuilder slot(String name, Object value, SlotValueType valueType) {
            this.additionalObjects.put("$.request.intent.slots." + name,
                                       new Slot(name, value, valueType));
            return this;
        }

        /**
         * @deprecated Use {@link #requestBodyBuilder(ObjectMapper)} or {@link #requestBodyBuilder(String,
         * ObjectMapper)} instead of this.
         */
        @Deprecated
        public RequestBodyBuilder configuration(Configuration configuration) {
            this.configuration = configuration;
            return this;
        }

        public RequestBodyBuilder add(String path, Object value) {
            if (path == null || path.isEmpty()) {
                throw new IllegalArgumentException("path should not be blank.");
            }
            this.additionalArrays.compute(path, (key, val) -> {
                List<Object> list = val != null ? val : new ArrayList<>();
                list.add(value);
                return list;
            });
            return this;
        }

        public RequestBodyBuilder put(String path, Object value) {
            if (path == null || path.isEmpty()) {
                throw new IllegalArgumentException("path should not be blank.");
            }
            this.additionalObjects.put(path, value);
            return this;
        }

        public RequestBodyBuilder remove(String path) {
            if (path == null || path.isEmpty()) {
                throw new IllegalArgumentException("path should not be blank.");
            }
            this.removePaths.add(path);
            return this;
        }

        public RequestBodyBuilder customize(RequestBodyBuilderCustomizer customizer) {
            customizer.custom(this);
            return this;
        }

        @SuppressWarnings("rawTypes")
        public String build() {
            Configuration configuration = getConfiguration();

            try (InputStream in = CEKRequestGenerator.class.getClassLoader().getResourceAsStream(this.path)) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(in, this.resourceCharset))) {
                    String body = reader.lines().collect(Collectors.joining("\n"));

                    for (Map.Entry<String, Object> entry : this.placeholder.entrySet()) {
                        body = body.replaceAll(Pattern.quote("${" + entry.getKey() + "}"),
                                               entry.getValue().toString());
                    }

                    if (!this.additionalArrays.isEmpty()) {
                        DocumentContext documentContext = JsonPath.using(configuration).parse(body);
                        this.additionalArrays.forEach((path, list) -> {
                            String cleanPath = cleanPath(path);
                            initPathIfAbsent(documentContext, cleanPath + "[]");
                            list.forEach(val -> documentContext.add(cleanPath, val));
                        });
                        body = documentContext.jsonString();
                    }

                    if (!this.additionalObjects.isEmpty()) {
                        DocumentContext documentContext = JsonPath.using(configuration).parse(body);
                        this.additionalObjects.forEach((path, value) -> {
                            String cleanPath = cleanPath(path);
                            initPathIfAbsent(documentContext, cleanPath);
                            documentContext.put(pathWithoutKey(cleanPath), key(cleanPath), value);
                        });
                        body = documentContext.jsonString();
                    }

                    if (!this.removePaths.isEmpty()) {
                        DocumentContext documentContext = JsonPath.using(configuration).parse(body);
                        this.removePaths.stream()
                                        .map(RequestBodyBuilder::cleanPath)
                                        .forEach(documentContext::delete);
                        body = documentContext.jsonString();
                    }

                    return body.trim() + "\n";
                }

            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }
        }

        private Configuration getConfiguration() {
            if (this.configuration == null) {
                this.configuration =
                        Configuration.builder()
                                     .mappingProvider(new JacksonMappingProvider(this.objectMapper))
                                     .jsonProvider(new JacksonJsonProvider(this.objectMapper))
                                     .build();
            }

            return this.configuration;
        }

        @SuppressWarnings("rawTypes")
        private static void initPathIfAbsent(DocumentContext documentContext, String path) {
            String[] keys = cleanPath(path).split("\\.");
            StringBuilder currentPathBuilder = new StringBuilder("$");
            for (int i = 0; i < keys.length - 1; i++) {
                String key = keys[i + 1];
                String currentPath = currentPathBuilder.toString();
                Map map = documentContext.read(currentPath, Map.class);
                if (!map.containsKey(key)) {
                    if (key.endsWith("[]")) {
                        documentContext.put(currentPath, key.substring(0, key.length() - 2), new ArrayList<>());
                    } else {
                        documentContext.put(currentPath, key, new LinkedHashMap<>());
                    }
                }
                currentPathBuilder.append(".").append(key);
            }
        }

        private static String cleanPath(String path) {
            if (path.startsWith("$.")) {
                return path;
            }
            if (path.startsWith(".")) {
                return "$" + path;
            }
            return "$." + path;
        }

        private static String pathWithoutKey(String path) {
            String suffix = "." + key(path);
            int suffixStartIndex = path.lastIndexOf(suffix);
            return path.substring(0, suffixStartIndex);
        }

        private static String key(String path) {
            String[] split = path.split("\\.");
            return split[split.length - 1];
        }

    }

    @FunctionalInterface
    public interface RequestBodyBuilderCustomizer {

        void custom(RequestBodyBuilder builder);

    }

    @Data
    @AllArgsConstructor
    private static class Slot {

        private String name;
        private Object value;
        private Object unit;
        private SlotValueType valueType;

        Slot(String name, Object value, Object unit) {
            this.name = name;
            this.value = value;
            this.unit = unit;
        }

        Slot(String name, Object value, SlotValueType valueType) {
            this.name = name;
            this.value = value;
            this.valueType = valueType;
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        Slot(String name, Object value) {
            this.name = name;
            this.value = value;
            if (value != null) {
                Arrays.stream(SlotValueType.values())
                      .filter(valueType -> {
                          if (valueType.getValueType().isAssignableFrom(value.getClass())) {
                              if (value instanceof SlotValueInterval) {
                                  Class<?> genericType = ((SlotValueInterval) value).getStart().getClass();
                                  return valueType.getGenericType().isAssignableFrom(genericType);
                              }
                              return true;
                          }
                          return false;
                      })
                      .findFirst()
                      .ifPresent(valueType -> {
                          if (this.value instanceof TemporalAccessor) {
                              this.value = valueType.format((TemporalAccessor) this.value);
                          } else if (this.value instanceof SlotValueInterval) {
                              this.value = valueType.format((SlotValueInterval) this.value);
                          }
                          this.valueType = valueType;
                      });
            }
        }
    }

}
