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

package com.linecorp.clova.extension.boot.handler;

import java.io.Serializable;

import org.springframework.util.Assert;

import com.linecorp.clova.extension.boot.util.StringUtils;

import lombok.Builder;
import lombok.Data;

/**
 * A class that contains a key to handle by request name.
 */
@Data
@Builder
public class CEKRequestKey implements Serializable {

    private static final long serialVersionUID = 1L;

    private String key;

    /**
     * Returns whether the specified requestName matches the key this instance holds.
     *
     * @param requestName CEK Request name.
     * @return {@code true} if the specified requestName matches the key this instance holds.
     */
    public boolean matches(String requestName) {
        if (StringUtils.isBlank(this.key) && StringUtils.isBlank(requestName)) {
            return true;
        }
        Assert.isTrue(StringUtils.isNotBlank(requestName), "requestName should not be blank.");
        if (this.key.equals("*")) {
            return true;
        }
        return requestName.equals(this.key);
    }

}
