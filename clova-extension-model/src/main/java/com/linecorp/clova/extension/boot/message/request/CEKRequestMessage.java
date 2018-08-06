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

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.type.TypeFactory;

import lombok.Data;

/**
 * Represents a Clova custom extension message as an object.
 * <p>
 * CEKRequestMessage is bound by the jackson's {@code ObjectMapper} from the http request body of the CEK request message.
 * <pre><code>
 * &#64;Autowired
 * ObjectMapper objectMapper;
 *
 * CEKRequestMessage message = objectMapper.readValue(requestBody, CEKRequestMessage.class);
 * </code></pre>
 */
@Data
public class CEKRequestMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotEmpty
    private Map<String, Object> context;

    @NotNull
    @Valid
    @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
    @JsonTypeIdResolver(CEKRequestTypeIdResolver.class)
    private CEKRequest request;

    @NotNull
    @Valid
    private Session session;

    @NotBlank
    private String version;

    @Data
    public static class Session implements Serializable {

        private static final long serialVersionUID = 1L;

        @NotNull
        @JsonProperty("new")
        private Boolean created;
        @NotNull
        private Map<String, Object> sessionAttributes;
        @NotBlank
        private String sessionId;
        @NotNull
        @Valid
        private User user;

        public void toReadOnly() {
            this.sessionAttributes = this.sessionAttributes != null
                                     ? Collections.unmodifiableMap(sessionAttributes)
                                     : Collections.emptyMap();
        }

        /**
         * The current requesting user linked to the device.
         */
        @Data
        public static class User implements Serializable {

            private static final long serialVersionUID = 1L;

            @NotBlank
            private String userId;
            private String accessToken;

        }

    }

    /**
     * {@link TypeIdResolver} implementation that converts between the concrete class of {@link CEKRequest} and
     * custom extension request messages.
     * It uses {@code request.type} to determine class.
     */
    public static class CEKRequestTypeIdResolver implements TypeIdResolver {

        private JavaType baseType;

        @Override
        public void init(JavaType baseType) {
            this.baseType = baseType;
        }

        @Override
        public String idFromValue(Object value) {
            return idFromValueAndType(value, value.getClass());
        }

        @Override
        public String idFromValueAndType(Object value, Class<?> suggestedType) {
            if (value instanceof CEKRequest) {
                CEKRequest request = (CEKRequest) value;
                return request.getType().getValue();
            }
            return null;
        }

        @Override
        public String idFromBaseType() {
            if (CEKRequest.class.isAssignableFrom(this.baseType.getRawClass())) {
                try {
                    CEKRequest request =
                            (CEKRequest) this.baseType.getRawClass().getDeclaredConstructor().newInstance();
                    return request.getType().getValue();
                } catch (Exception e) {
                    throw new IllegalArgumentException(e);
                }
            }
            return null;
        }

        @Override
        public JavaType typeFromId(DatabindContext context, String id) throws IOException {
            TypeFactory typeFactory =
                    (context != null) ? context.getTypeFactory() : TypeFactory.defaultInstance();
            return Arrays.stream(RequestType.values())
                         .filter(type -> type.getValue().equals(id))
                         .findFirst()
                         .map(type -> typeFactory.constructType(type.getHoldClass()))
                         .orElseThrow(() -> new IllegalArgumentException(
                                 "Can't find request class [id: " + id + "]"));
        }

        @Override
        public String getDescForKnownTypeIds() {
            return null;
        }

        @Override
        public JsonTypeInfo.Id getMechanism() {
            return JsonTypeInfo.Id.CUSTOM;
        }
    }
}
