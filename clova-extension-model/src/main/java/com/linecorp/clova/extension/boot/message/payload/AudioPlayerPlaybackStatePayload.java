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

package com.linecorp.clova.extension.boot.message.payload;

import static lombok.AccessLevel.PRIVATE;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A {@link Payload} for {@code AudioPlayer} status notification event.
 * <p>
 * The expected event names are as follows.
 * <ul>
 * <li>{@code AudioPlayer.PlayStarted}
 * <li>{@code AudioPlayer.PlayPaused}
 * <li>{@code AudioPlayer.PlayResumed}
 * <li>{@code AudioPlayer.PlayFinished}
 * <li>{@code AudioPlayer.PlayStopped}
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = PRIVATE)
public class AudioPlayerPlaybackStatePayload implements Payload {

    private static final long serialVersionUID = 1L;

    @NotBlank
    private String token;
    @NotNull
    private Long offsetInMilliseconds;

}
