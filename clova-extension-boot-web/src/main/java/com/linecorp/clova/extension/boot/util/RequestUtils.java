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

package com.linecorp.clova.extension.boot.util;

import javax.servlet.http.HttpServletRequest;

import org.springframework.util.Assert;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.linecorp.clova.extension.boot.message.request.CEKRequest;
import com.linecorp.clova.extension.boot.message.request.EventRequest;
import com.linecorp.clova.extension.boot.message.request.RequestType;

import lombok.experimental.UtilityClass;

/**
 * Utilities for Clova or CEK request.
 */
@UtilityClass
public class RequestUtils {

    public static final String REQUEST_TYPE_ATTR_KEY = "cek.requestType";
    public static final String REQUEST_ID_ATTR_KEY = "cek.requestId";
    public static final String REQUEST_NAME_ATTR_KEY = "cek.requestName";
    public static final String REQUEST_BODY_JSON_ATTR_KEY = "cek.requestBodyJson";

    /**
     * Gets current {@link HttpServletRequest request} from {@link ThreadLocal}.
     *
     * @return current {@link HttpServletRequest request}
     */
    public static HttpServletRequest getCurrentHttpRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        Assert.isInstanceOf(ServletRequestAttributes.class, requestAttributes);

        return ((ServletRequestAttributes) requestAttributes).getRequest();
    }

    /**
     * Sets {@link RequestType request type} to request attribute.
     *
     * @param request     {@link HttpServletRequest}
     * @param requestType {@link RequestType}
     */
    public static void setRequestType(HttpServletRequest request, RequestType requestType) {
        request.setAttribute(REQUEST_TYPE_ATTR_KEY, requestType);
    }

    /**
     * Gets {@link RequestType request type} from request attribute.
     *
     * @param request {@link HttpServletRequest}
     * @return {@link RequestType request type}
     */
    public static RequestType getRequestType(HttpServletRequest request) {
        return (RequestType) request.getAttribute(REQUEST_TYPE_ATTR_KEY);
    }

    /**
     * Sets {@link CEKRequest#getName() request name} to request attribute.
     *
     * @param request     {@link HttpServletRequest}
     * @param requestName request name
     */
    public static void setRequestName(HttpServletRequest request, String requestName) {
        request.setAttribute(REQUEST_NAME_ATTR_KEY, requestName);
    }

    /**
     * Gets {@link CEKRequest#getName() request name} from request attribute.
     *
     * @param request {@link HttpServletRequest}
     * @return request name
     */
    public static String getRequestName(HttpServletRequest request) {
        return (String) request.getAttribute(REQUEST_NAME_ATTR_KEY);
    }

    /**
     * Sets {@link EventRequest#requestId request id} to request attribute.
     *
     * @param request   {@link HttpServletRequest}
     * @param requestId request id
     */
    public static void setRequestId(HttpServletRequest request, String requestId) {
        request.setAttribute(REQUEST_ID_ATTR_KEY, requestId);
    }

    /**
     * Gets {@link EventRequest#requestId request id} from request attribute.
     *
     * @param request {@link HttpServletRequest}
     * @return request id, may be null if the request type is not event.
     */
    public static String getRequestId(HttpServletRequest request) {
        return (String) request.getAttribute(REQUEST_ID_ATTR_KEY);
    }

    /**
     * Sets request json body to request attribute.
     *
     * @param request {@link HttpServletRequest}
     * @param json    request json body
     */
    public static void setRequestBodyJson(HttpServletRequest request, String json) {
        request.setAttribute(REQUEST_BODY_JSON_ATTR_KEY, json);
    }

    /**
     * Gets request json body from request attribute.
     *
     * @param request {@link HttpServletRequest}
     * @return json request json body
     */
    public static String getRequestBodyJson(HttpServletRequest request) {
        return (String) request.getAttribute(REQUEST_BODY_JSON_ATTR_KEY);
    }

}
