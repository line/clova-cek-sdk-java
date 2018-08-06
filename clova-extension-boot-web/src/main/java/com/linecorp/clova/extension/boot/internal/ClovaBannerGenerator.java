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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;

/**
 * Clova Banner Generator.
 * <p>
 * This replaces the Spring Boot banner with the Clova banner as default for each extension.
 * When compiling this library, the generation process is called.
 * <p>
 * This class is for internal processes, NOT for extensions.
 */
class ClovaBannerGenerator {

    static void generate(String version) throws IOException {
        Resource source = new ClassPathResource("banner.txt");
        try (InputStream inputStream = source.getInputStream()) {
            String contents = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
            contents = contents.replace("${clova-extension-boot.version}", " (v" + version + ")");
            try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(source.getFile()),
                                                                    StandardCharsets.UTF_8)) {
                writer.write(contents);
                writer.flush();
            }
        }
    }

}
