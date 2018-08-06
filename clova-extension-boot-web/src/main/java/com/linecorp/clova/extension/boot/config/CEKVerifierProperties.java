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

package com.linecorp.clova.extension.boot.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.linecorp.clova.extension.boot.verifier.CEKRequestExtensionIdVerifier;
import com.linecorp.clova.extension.boot.verifier.CEKRequestSignatureVerifier;
import com.linecorp.clova.extension.boot.verifier.CEKRequestVerifier;

import lombok.Data;

/**
 * Configuration properties for {@link CEKRequestVerifier}s.
 */
@Data
@ConfigurationProperties("cek.verifier")
public class CEKVerifierProperties {

    /**
     * Configuration properties for {@link CEKRequestSignatureVerifier}.
     */
    private Signature signature = new Signature();

    /**
     * Configuration properties for {@link CEKRequestExtensionIdVerifier}.
     */
    private ExtensionId extensionId = new ExtensionId();

    @Data
    public static class Signature {
        /**
         * Whether to use {@link CEKRequestSignatureVerifier}.
         */
        private boolean enabled = true;
        /**
         * The public key resource.
         * <p>
         * see: META-INF/additional-spring-configuration-metadata.json
         */
        private Resource publicKey = new ClassPathResource("key/signature-public-key.pem");

    }

    @Data
    public static class ExtensionId {
        /**
         * Whether to use {@link CEKRequestExtensionIdVerifier}.
         * <p>
         * If {@link #id id} is empty, the verifier will not verify even if this value is {@code true}.
         */
        private boolean enabled = true;

        /**
         * The expected application ID.
         * <p>
         * If it does not match any of these id, it is regarded as an invalid request.
         * If more than one is specified, the request is verified if at least one id matches.
         */
        private List<String> id = new ArrayList<>();

    }

}
