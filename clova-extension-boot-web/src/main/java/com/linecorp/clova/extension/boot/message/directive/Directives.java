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

package com.linecorp.clova.extension.boot.message.directive;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;

import com.linecorp.clova.extension.boot.message.payload.AudioPlayerPlayPayload;
import com.linecorp.clova.extension.boot.message.payload.AudioPlayerStreamPayload;
import com.linecorp.clova.extension.boot.util.RequestUtils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Classes that have static utility methods for {@link Directive} instances.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Directives {

    /**
     * Static utility methods for {@code AudioPlayer} {@link Directive} instances.
     */
    @Slf4j
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class AudioPlayer {

        /**
         * Creates {@code AudioPlayer.Play} {@link Directive} with the specified payload.
         *
         * @param payload directive payload
         * @return directive instance
         */
        public static Directive play(AudioPlayerPlayPayload payload) {
            return Directive.builder()
                            .header(Header.builder()
                                          .namespace("AudioPlayer")
                                          .name("Play")
                                          .dialogRequestId(getDialogRequestId(log))
                                          .build())
                            .payload(payload)
                            .build();
        }

        /**
         * Creates {@code AudioPlayer.StreamDeliver} {@link Directive} with the specified payload.
         *
         * @param payload directive payload
         * @return directive instance
         */
        public static Directive streamDeliver(AudioPlayerStreamPayload payload) {
            return Directive.builder()
                            .header(Header.builder()
                                          .namespace("AudioPlayer")
                                          .name("StreamDeliver")
                                          .dialogRequestId(getDialogRequestId(log))
                                          .build())
                            .payload(payload)
                            .build();
        }

    }

    /**
     * Static utility methods for {@code PlaybackController} {@link Directive} instances.
     */
    @Slf4j
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class PlaybackController {

        /**
         * Creates {@code PlaybackController.Pause} {@link Directive}.
         *
         * @return directive instance
         */
        public static Directive pause() {
            return Directive.builder()
                            .header(Header.builder()
                                          .namespace("PlaybackController")
                                          .name("Pause")
                                          .dialogRequestId(getDialogRequestId(log))
                                          .build())
                            .build();
        }

        /**
         * Creates {@code PlaybackController.Resume} {@link Directive}.
         *
         * @return directive instance
         */
        public static Directive resume() {
            return Directive.builder()
                            .header(Header.builder()
                                          .namespace("PlaybackController")
                                          .name("Resume")
                                          .dialogRequestId(getDialogRequestId(log))
                                          .build())
                            .build();
        }

        /**
         * Creates {@code PlaybackController.Stop} {@link Directive}.
         *
         * @return directive instance
         */
        public static Directive stop() {
            return Directive.builder()
                            .header(Header.builder()
                                          .namespace("PlaybackController")
                                          .name("Stop")
                                          .dialogRequestId(getDialogRequestId(log))
                                          .build())
                            .build();
        }

    }

    private static String getDialogRequestId(Logger log) {
        HttpServletRequest request;
        try {
            request = RequestUtils.getCurrentHttpRequest();
        } catch (Exception e) {
            log.debug("Cannot get current request.", e);
            return null;
        }
        return RequestUtils.getRequestId(request);
    }

}
