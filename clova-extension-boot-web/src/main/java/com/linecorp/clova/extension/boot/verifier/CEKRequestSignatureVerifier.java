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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.io.Resource;

import com.linecorp.clova.extension.boot.message.context.SystemContext;
import com.linecorp.clova.extension.boot.message.request.CEKRequestMessage;
import com.linecorp.clova.extension.boot.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * A {@link CEKRequestVerifier Verifier} for a signed CEK request.
 * <p>
 * Verifies the request body using a signature obtained from the HTTP header.
 */
@Slf4j
public class CEKRequestSignatureVerifier implements CEKRequestVerifier {

    static final String CLOVA_SIGNATURE_REQUEST_HEADER = "SignatureCEK";

    private static final String CLOVA_SIGNATURE_ALGORITHM = "SHA256withRSA";
    private static final String CLOVA_SIGNATURE_KEY_TYPE = "RSA";

    private static final String PEM_BEGIN_PREFIX = "-----BEGIN ";
    private static final String PEM_END_PREFIX = "-----END ";

    private final Resource publicKeyResource;
    private PublicKey publicKey;

    public CEKRequestSignatureVerifier(Resource keyResource)
            throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        this.publicKeyResource = keyResource;
        loadPublicKey();
        log.info("Loaded the public key from {}", this.publicKeyResource);
    }

    @Override
    public void verify(HttpServletRequest request, CEKRequestMessage requestMessage, String requestJson,
                       SystemContext system) {
        String baseEncoded64Signature = request.getHeader(CLOVA_SIGNATURE_REQUEST_HEADER);
        if (StringUtils.isBlank(baseEncoded64Signature)) {
            throw new SecurityException(CLOVA_SIGNATURE_REQUEST_HEADER + " is missing.");
        }

        try {
            Signature signature = Signature.getInstance(CLOVA_SIGNATURE_ALGORITHM);
            signature.initVerify(this.publicKey);
            signature.update(requestJson.getBytes(StandardCharsets.UTF_8));

            if (signature.verify(
                    Base64.getDecoder().decode(baseEncoded64Signature.getBytes(StandardCharsets.UTF_8)))) {
                return;
            }
            throw new SecurityException("Failed to verify the signature for the provided request.");
        } catch (GeneralSecurityException e) {
            throw new SecurityException("Failed to verify the signature for the provided request.", e);
        }
    }

    private void loadPublicKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(readPem(this.publicKeyResource));
        KeyFactory keyFactory = KeyFactory.getInstance(CLOVA_SIGNATURE_KEY_TYPE);
        this.publicKey = keyFactory.generatePublic(keySpec);
    }

    private static byte[] readPem(Resource keyResource) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(keyResource.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean started = false;
            while ((line = reader.readLine()) != null && !line.contains(":")) {
                if (!started) {
                    started = line.startsWith(PEM_BEGIN_PREFIX);
                    continue;
                }
                if (line.startsWith(PEM_END_PREFIX)) {
                    break;
                }
                sb.append(line);
            }
        }
        return Base64.getDecoder().decode(sb.toString());
    }

}
