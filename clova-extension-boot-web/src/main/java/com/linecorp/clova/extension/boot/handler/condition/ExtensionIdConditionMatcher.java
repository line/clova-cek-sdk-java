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

package com.linecorp.clova.extension.boot.handler.condition;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import org.springframework.util.Assert;

import com.linecorp.clova.extension.boot.message.context.SystemContext;
import com.linecorp.clova.extension.boot.message.request.CEKRequestMessage;
import com.linecorp.clova.extension.boot.util.StringUtils;

/**
 * A {@link CEKHandleConditionMatcher} for Extension ID.
 */
public class ExtensionIdConditionMatcher implements CEKHandleConditionMatcher {

    private final Collection<String> extensionIds;

    public ExtensionIdConditionMatcher(Collection<String> extensionIds) {
        Assert.isTrue(!extensionIds.isEmpty(), "extensionIds should not be empty.");
        Assert.isTrue(extensionIds.stream().allMatch(StringUtils::isNotBlank),
                      "extensionId should not be blank.");
        this.extensionIds = extensionIds;
    }

    @Override
    public boolean match(HttpServletRequest request, CEKRequestMessage requestMessage, SystemContext system) {
        String requestExtensionId = system.getApplication().getApplicationId();

        return this.extensionIds.stream()
                                .anyMatch(extensionId -> {
                                    if (extensionId.equals("*")) {
                                        return true;
                                    }
                                    if (StringUtils.isBlank(requestExtensionId)) {
                                        return false;
                                    }
                                    if (extensionId.endsWith(".*")) {
                                        return requestExtensionId.startsWith(
                                                extensionId.substring(0, extensionId.length() - 2));
                                    }
                                    if (extensionId.startsWith("*.")) {
                                        return requestExtensionId.endsWith(extensionId.substring(2));
                                    }
                                    return requestExtensionId.equals(extensionId);
                                });
    }

    @Override
    public String toString() {
        return "extensionIds:" + extensionIds;
    }

}
