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

import javax.servlet.http.HttpServletRequest;

import com.linecorp.clova.extension.boot.message.context.SystemContext;
import com.linecorp.clova.extension.boot.message.request.CEKRequestMessage;

/**
 * An interface for determining if there is a match with the Handler's execution conditions.
 * <p>
 * If you would like to add execution conditions to the Handler, define a class that has implemented the interface and
 * register to {@link com.linecorp.clova.extension.boot.handler.CEKHandlerMethod#conditionMatchers}.
 * The annotation that indicates the execution conditions added to the Handler will add
 * the {@link com.linecorp.clova.extension.boot.handler.annnotation} package.
 */
public interface CEKHandleConditionMatcher {

    /**
     * Return true when the Handler should process the request.
     * <p>
     *
     * @param request        {@link HttpServletRequest}
     * @param requestMessage {@link CEKRequestMessage}
     * @param system         {@link System}
     * @return true if the Handler should process the request.
     */
    boolean match(HttpServletRequest request, CEKRequestMessage requestMessage, SystemContext system);
}
