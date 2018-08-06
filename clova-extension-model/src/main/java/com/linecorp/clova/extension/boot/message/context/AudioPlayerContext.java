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

import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.linecorp.clova.extension.boot.message.model.AudioStreamInfoObject;

import lombok.Data;

/**
 * An object that contains the context information of the device audio player.
 */
@Data
public class AudioPlayerContext implements ContextProperty {

    private static final long serialVersionUID = 1L;

    private Long offsetInMilliseconds;
    @NotNull
    private PlayerActivity playerActivity;
    @NotNull
    @Valid
    private AudioStreamInfoObject stream;
    private Long totalInMilliseconds;

    public enum PlayerActivity {
        IDLE,
        PLAYING,
        PAUSED,
        STOPPED
    }

    @AssertTrue
    @JsonIgnore
    public boolean isValidOffsetInMilliseconds() {
        if (playerActivity == PlayerActivity.IDLE) {
            return true;
        }
        return offsetInMilliseconds != null;
    }

}
