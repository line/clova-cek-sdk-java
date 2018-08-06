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

package com.linecorp.clova.extension.boot;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;

import com.linecorp.clova.extension.boot.util.StringUtils;

/**
 * Class that exposes Clova Extension Boot version.
 * <p>
 * Gets the version from {@code version.txt} that included in this package.
 */
public class ClovaExtensionBootVersion {

    private static final String VERSION = loadVersion();

    /**
     * Gets Clova Extension Boot version
     *
     * @return Clova Extension Boot version
     */
    public static String getVersion() {
        return VERSION;
    }

    public static Resource getVersionTextResource() {
        String versionTextFilePath =
                ClovaExtensionBootVersion.class.getPackage().getName()
                                               .replaceAll("\\.", "/") + "/version.txt";
        return new ClassPathResource(versionTextFilePath);
    }

    private static String loadVersion() {
        return Optional.ofNullable(ClovaExtensionBootVersion.class.getPackage())
                       .map(Package::getImplementationVersion)
                       .filter(StringUtils::isNotBlank)
                       .orElseGet(() -> {
                           Resource resource = getVersionTextResource();
                           try {
                               return StreamUtils.copyToString(getVersionTextResource().getInputStream(),
                                                               StandardCharsets.UTF_8)
                                                 .trim();
                           } catch (IOException e) {
                               throw new IllegalStateException(
                                       String.format("Not found version.txt. path:[%s]", resource));
                           }
                       });
    }

}
