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

package com.linecorp.clova.extension.boot.internal;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Resources generator.
 * <p>
 * When compiling this library, the generation process is called.
 * <p>
 * This class is for internal processes, NOT for extensions.
 */
public class ClovaExtensionBootResourcesGenerator {

    private static final Pattern ARGUMENT_PATTERN = Pattern.compile("--(?<name>[\\d\\w]+)=(?<value>.+)");

    /**
     * Entry point of {@link ClovaExtensionBootResourcesGenerator}.
     */
    public static void main(String[] args) throws Exception {
        Map<String, String> parameters = parseArgs(args);
        String version = parameters.get("version");

        ClovaExtensionBootVersionTextGenerator.generate(version);
        ClovaBannerGenerator.generate(version);
    }

    private static Map<String, String> parseArgs(String[] args) {
        if (args == null || args.length == 0) {
            return Collections.emptyMap();
        }
        return Arrays.stream(args)
                     .map(arg -> {
                         Matcher matcher = ARGUMENT_PATTERN.matcher(arg);
                         if (!matcher.matches()) {
                             return null;
                         }
                         return new AbstractMap.SimpleEntry<>(
                                 matcher.group("name"), matcher.group("value"));
                     })
                     .filter(Objects::nonNull)
                     .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

}
