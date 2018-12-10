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

import javax.servlet.http.HttpServletRequest;

import com.linecorp.clova.extension.boot.autoconfigure.CEKWebAutoConfiguration;
import com.linecorp.clova.extension.boot.handler.CEKRequestHandlerDispatcher;
import com.linecorp.clova.extension.boot.message.context.SystemContext;
import com.linecorp.clova.extension.boot.message.request.CEKRequestMessage;

/**
 * CEK request verifier.
 * <p>
 * The class implements this interface are called automatically, if it's register to Spring DI container as
 * Bean.
 *
 * @see CEKRequestSignatureVerifier
 * @see CEKRequestHandlerDispatcher
 * @see CEKWebAutoConfiguration#cekRequestHandlerDispatcher(com.linecorp.clova.extension.boot.handler.CEKRequestMappingHandlerMapping,
 * org.springframework.beans.factory.ObjectProvider, org.springframework.beans.factory.ObjectProvider,
 * org.springframework.beans.factory.ObjectProvider, org.springframework.beans.factory.ObjectProvider)
 */
public interface CEKRequestVerifier {

    /**
     * Verifies CEK request.
     *
     * @param request        {@link HttpServletRequest}
     * @param requestMessage {@link CEKRequestMessage}
     * @param requestJson    request body, should be json text
     * @param system         the context information of the client system
     * @throws SecurityException the request is invalid.
     */
    void verify(HttpServletRequest request, CEKRequestMessage requestMessage, String requestJson,
                SystemContext system) throws SecurityException;

}
