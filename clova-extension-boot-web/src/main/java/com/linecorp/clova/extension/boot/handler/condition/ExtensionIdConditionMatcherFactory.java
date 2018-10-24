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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.util.Assert;

import com.linecorp.clova.extension.boot.handler.annnotation.ExtensionIdCondition;

/**
 * {@link CEKHandleConditionMatcherFactory} to create {@link ExtensionIdConditionMatcher}.
 *
 * @see CEKHandleConditionMatcherFactory
 * @see ExtensionIdConditionMatcher
 * @see ExtensionIdCondition
 */
public class ExtensionIdConditionMatcherFactory
        implements CEKHandleConditionMatcherFactory<ExtensionIdConditionMatcher, ExtensionIdCondition> {

    @Override
    public ExtensionIdConditionMatcher create(Collection<ExtensionIdCondition> annotations) {
        Assert.isTrue(!annotations.isEmpty(), "annotations should not be empty.");

        List<String> extensionIds = annotations.stream()
                                               .map(ExtensionIdCondition::extensionId)
                                               .flatMap(Arrays::stream)
                                               .collect(Collectors.toList());

        return new ExtensionIdConditionMatcher(extensionIds);
    }

}
