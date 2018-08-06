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

package com.linecorp.clova.extension.boot.controller.advice;

import java.lang.reflect.UndeclaredThrowableException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.linecorp.clova.extension.boot.controller.CEKHandleIntentController;
import com.linecorp.clova.extension.boot.message.response.CEKResponse;
import com.linecorp.clova.extension.boot.message.response.CEKResponseMessage;
import com.linecorp.clova.extension.boot.message.speech.OutputSpeech;
import com.linecorp.clova.extension.boot.message.speech.OutputSpeechGenerator;
import com.linecorp.clova.extension.boot.util.LogLevel;
import com.linecorp.clova.extension.boot.util.LogUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * A {@link RestControllerAdvice} for {@link CEKHandleIntentController}.
 */
@RequiredArgsConstructor
@RestControllerAdvice(assignableTypes = CEKHandleIntentController.class)
@Slf4j
public class CEKHandleIntentControllerAdvice {

    private final OutputSpeechGenerator outputSpeechGenerator;

    /**
     * Handles most errors.
     * <p>
     * Outputs an error log and generates an {@link OutputSpeech} from the exception.
     *
     * @param t {@link Throwable}
     * @param request {@link HttpServletRequest}
     *
     * @return A {@link CEKResponseMessage} includes {@link OutputSpeech} with HTTP status 200.
     */
    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.OK)
    public CEKResponseMessage handle(Throwable t, HttpServletRequest request) {
        Throwable cleanedT = cleanedThrowable(t);
        LogLevel logLevel = AnnotationUtils.getAnnotation(cleanedT.getClass(), LogLevel.class);
        LogUtils.logging(log, cleanedT, logLevel);

        CEKResponse response = new CEKResponse();
        response.setOutputSpeech(outputSpeechGenerator.generateFrom(cleanedT));

        return new CEKResponseMessage(response);
    }

    /**
     * Handles errors thrown by the verifier.
     * <p>
     * Outputs an error log, and returns an empty response body.
     *
     * @param e {@link SecurityException} with HTTPS status 400.
     */
    @ExceptionHandler(SecurityException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handle(SecurityException e) {
        log.warn(e.getMessage(), e);
    }

    private Throwable cleanedThrowable(Throwable t) {
        if (t instanceof UndeclaredThrowableException) {
            return t.getCause();
        }
        return t;
    }

}
