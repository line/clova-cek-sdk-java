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

package com.linecorp.clova.extension.boot.session;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.validation.Valid;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.linecorp.clova.extension.boot.message.request.CEKRequestMessage.Session;
import com.linecorp.clova.extension.boot.message.response.CEKResponse;

import lombok.Getter;
import lombok.NonNull;

/**
 * Component for operation of session attributes.
 * <p>
 * Any CEK handler methods get this instance from method argument.
 * This class instance is created by CEK API request.
 * <p>
 * You can operate session attributes via this class, and the result is effects the CEK response
 * unless {@link CEKResponse#shouldEndSession} is set {@code true}.
 */
public class SessionHolder {

    private final Map<String, Object> sessionCache = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper;

    @Getter
    @Valid
    private final Session session;
    @Getter
    private final Map<String, Object> sessionAttributes;

    /**
     * Constructs a new instance with the specified session.
     * <p>
     * This class is required {@link ObjectMapper} to deserialize session attribute value.
     *
     * @param objectMapper {@link ObjectMapper}
     * @param session      {@link Session}
     */
    public SessionHolder(@NonNull ObjectMapper objectMapper, @NonNull Session session) {
        this.objectMapper = objectMapper;
        this.session = session;
        this.sessionAttributes = session.getSessionAttributes() != null
                                 ? new ConcurrentHashMap<>(session.getSessionAttributes())
                                 : new ConcurrentHashMap<>();
    }

    /**
     * Gets session attribute value named specified, with the specified type.
     * <p>
     * From the second call, this method returns cached value until the value is changed.
     *
     * @param name session attribute name
     * @param type session attribute value type
     * @param <T>  session attribute value type
     * @return session attribute value
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(@NonNull String name, @NonNull Class<T> type) {
        return (T) sessionCache.compute(name, (key, cachedVal) -> {
            if (cachedVal != null && type.isAssignableFrom(cachedVal.getClass())) {
                return cachedVal;
            }
            Object val = sessionAttributes.get(key);
            if (val == null) {
                return null;
            }
            return objectMapper.convertValue(val, type);
        });
    }

    /**
     * Gets session attribute value named specified, with the specified type.
     * <p>
     * It's deserialized using the specified {@link TypeReference type reference}.
     * <p>
     * This method always deserializes the specified value.
     *
     * @param name          session attribute name
     * @param typeReference type reference for deserialization
     * @param <T>           session attribute value type
     * @return session attribute value
     */
    public <T> T getAttribute(@NonNull String name, @NonNull TypeReference<T> typeReference) {
        Object val = sessionAttributes.get(name);
        if (val == null) {
            return null;
        }

        return objectMapper.convertValue(val, objectMapper.getTypeFactory().constructType(typeReference));
    }

    /**
     * Sets session attribute value.
     *
     * @param name  session attribute name
     * @param value session attribute value
     */
    public void setAttribute(@NonNull String name, Object value) {
        sessionAttributes.put(name, value);
        sessionCache.put(name, value);
    }

    /**
     * Removes session attribute value.
     *
     * @param name session attribute name
     */
    public void removeAttribute(@NonNull String name) {
        sessionAttributes.remove(name);
        sessionCache.remove(name);
    }

}
