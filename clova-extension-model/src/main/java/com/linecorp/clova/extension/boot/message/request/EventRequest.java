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
import java.time.OffsetDateTime;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import com.linecorp.clova.extension.boot.message.payload.MapPayload;
import com.linecorp.clova.extension.boot.message.payload.Payload;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Represents an {@code EventRequest} as an object.
 * <p>
 * The EventRequest-type request is used by the CEK to send client requests to the extension, e.g. an audio file started,
 * pause button tapped, etc.
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class EventRequest extends CEKRequest {

    private static final long serialVersionUID = 1L;

    @NotNull
    @Valid
    private Event event;
    @NotNull
    private String requestId;
    @NotNull
    private OffsetDateTime timestamp;

    public EventRequest() {
        super(RequestType.EVENT);
    }

    @Override
    public String getName() {
        if (event == null) {
            return null;
        }
        return event.getNamespace() + "." + event.getName();
    }

    @Data
    public static class Event implements Serializable {

        private static final long serialVersionUID = 1L;

        @NotBlank
        private String namespace;
        @NotBlank
        private String name;
        @NotNull
        @JsonDeserialize(using = PayloadMapDeserializer.class)
        private Payload payload;

        public static class PayloadMapDeserializer extends JsonDeserializer<Payload> {

            @Override
            public Payload deserialize(JsonParser p, DeserializationContext ctxt)
                    throws IOException, JsonProcessingException {
                Map<String, Object> mapValue = p.readValueAs(new TypeReference<Map<String, Object>>() {});
                MapPayload mapPayload = new MapPayload();
                mapPayload.putAll(mapValue);
                return mapPayload;
            }
        }

    }

}
