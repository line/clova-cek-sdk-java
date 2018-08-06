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

package com.linecorp.clova.extension.boot.message.context;

import java.io.Serializable;

import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * An object that contains the context information of the client system.
 */
@Data
public class SystemContext implements ContextProperty {

    private static final long serialVersionUID = 1L;

    @NotNull
    @Valid
    private Application application;
    @NotNull
    @Valid
    private Device device;
    @NotNull
    @Valid
    private User user;

    /**
     * An object that contains the context information of the extension.
     */
    @Data
    public static class Application implements Serializable {

        private static final long serialVersionUID = 1L;

        @NotBlank
        private String applicationId;
    }

    /**
     * An object that contains the context information of the client device.
     */
    @Data
    public static class Device implements Serializable {

        private static final long serialVersionUID = 1L;

        @NotBlank
        private String deviceId;
        @NotNull
        @Valid
        private Display display;

        @Data
        public static class Display implements Serializable {

            private static final long serialVersionUID = 1L;

            @Valid
            private ContentLayer contentLayer;
            private Integer dpi;
            private Orientation orientation;
            @NotNull
            private Size size;

            @AssertTrue
            @JsonIgnore
            public boolean isValidContentLayer() {
                if (size == null || size == Size.NONE) {
                    return true;
                }
                return contentLayer != null;
            }

            @AssertTrue
            @JsonIgnore
            public boolean isValidDpi() {
                if (size == null || size == Size.NONE) {
                    return true;
                }
                return dpi != null;
            }

            @Data
            public static class ContentLayer implements Serializable {

                private static final long serialVersionUID = 1L;

                @NotNull
                private Integer width;
                @NotNull
                private Integer height;
            }

            @RequiredArgsConstructor
            public enum Orientation {
                LANDSCAPE("landscape"),
                PORTRAIT("portrait");

                @Getter(onMethod = @__(@JsonValue))
                private final String value;
            }

            @RequiredArgsConstructor
            public enum Size {
                NONE("none"),
                S100("s100"),
                M100("m100"),
                L100("l100"),
                XL100("xl100"),
                CUSTOM("custom");

                @Getter(onMethod = @__(@JsonValue))
                private final String value;
            }
        }
    }

    /**
     * The default user linked to the device.
     */
    @Data
    public static class User implements Serializable {

        private static final long serialVersionUID = 1L;

        @NotBlank
        private String userId;
        private String accessToken;

    }

}
