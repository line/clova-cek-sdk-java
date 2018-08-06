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

package com.linecorp.clova.extension.boot.verifier;

import java.util.Collection;
import java.util.Collections;

import javax.servlet.http.HttpServletRequest;

import com.linecorp.clova.extension.boot.message.context.SystemContext;
import com.linecorp.clova.extension.boot.message.request.CEKRequestMessage;
import com.linecorp.clova.extension.boot.util.StringUtils;

public class CEKRequestExtensionIdVerifier implements CEKRequestVerifier {

    private final Collection<String> applicationIds;

    public CEKRequestExtensionIdVerifier(Collection<String> applicationIds) {
        this.applicationIds = Collections.unmodifiableCollection(applicationIds);
    }

    @Override
    public void verify(HttpServletRequest request, CEKRequestMessage requestMessage, String requestJson,
                       SystemContext system) throws SecurityException {
        if (this.applicationIds == null || this.applicationIds.isEmpty()) {
            return;
        }

        String applicationId = system.getApplication().getApplicationId();

        if (StringUtils.isBlank(applicationId)) {
            throw new SecurityException("applicationId is missing.");
        }

        if (!this.applicationIds.contains(applicationId)) {
            throw new SecurityException(String.format("[%s] is not expected applicationId.", applicationId));
        }
    }

}
