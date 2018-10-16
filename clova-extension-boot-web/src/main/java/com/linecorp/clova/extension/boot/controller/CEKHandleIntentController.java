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

package com.linecorp.clova.extension.boot.controller;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.linecorp.clova.extension.boot.config.CEKProperties;
import com.linecorp.clova.extension.boot.handler.CEKRequestProcessor;
import com.linecorp.clova.extension.boot.message.request.CEKRequestMessage;
import com.linecorp.clova.extension.boot.message.response.CEKResponseMessage;
import com.linecorp.clova.extension.boot.util.RequestUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * The controller for handling CEK Requests.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class CEKHandleIntentController {

    private final CEKRequestProcessor requestProcessor;
    private final ObjectMapper objectMapper;

    private final Locale defaultClientLocale;

    /**
     * The handler method for a CEK Request.
     *
     * @param request     {@link HttpServletRequest}
     * @param response    {@link HttpServletResponse}
     * @param requestBody The CEK Request body as text.
     * @return The CEK Response
     * @throws Throwable Any errors in this application process.
     * @see CEKProperties#apiPath
     */
    @PostMapping("${cek.api-path:/}")
    public CEKResponseMessage handle(HttpServletRequest request, HttpServletResponse response,
                                     @RequestBody String requestBody)
            throws Throwable {
        if (log.isDebugEnabled()) {
            log.debug("CEK Payload --> {}", requestBody);
        }
        RequestUtils.setRequestBodyJson(request, requestBody);
        CEKRequestMessage requestMessage = objectMapper.readValue(requestBody, CEKRequestMessage.class);
        LocaleContextHolder.setLocale(defaultClientLocale, true);

        return requestProcessor.process(request, response, requestMessage);
    }

}
