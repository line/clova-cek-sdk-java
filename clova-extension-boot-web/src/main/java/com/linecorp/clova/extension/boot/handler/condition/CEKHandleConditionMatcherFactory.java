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

import java.lang.annotation.Annotation;
import java.util.Collection;

/**
 * A factory interface to create {@link CEKHandleConditionMatcher} instance.
 *
 * @param <M> Type of {@link CEKHandleConditionMatcher} to be created.
 * @param <A> Type of additional condition to annotate to CEK Request Handler method.
 * @see com.linecorp.clova.extension.boot.handler.annnotation.CEKHandleCondition
 * @see com.linecorp.clova.extension.boot.handler.CEKRequestMappingHandlerMapping
 */
public interface CEKHandleConditionMatcherFactory<M extends CEKHandleConditionMatcher, A extends Annotation> {

    /**
     * Creates {@link CEKHandleConditionMatcher} instance.
     *
     * @param annotations used by {@link CEKHandleConditionMatcher} for matching.
     * @return {@link CEKHandleConditionMatcher} instance.
     */
    M create(Collection<A> annotations);

}
