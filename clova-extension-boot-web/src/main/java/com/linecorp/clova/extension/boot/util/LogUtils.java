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

import org.slf4j.Logger;
import org.springframework.core.annotation.AnnotationUtils;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

/**
 * Logging utilities.
 */
@UtilityClass
public class LogUtils {

    /**
     * Logs to the appropriate level according to the specified exception.
     * <p>
     * The exception should be annotated with {@link LogLevel @LogLevel}.
     * Log with ERROR Level if the exception is not annotated with {@link LogLevel @LogLevel}.
     *
     * @param log {@link Logger}
     * @param t   error to log
     */
    public static void logging(@NonNull Logger log, @NonNull Throwable t) {
        LogLevel logLevel = AnnotationUtils.getAnnotation(t.getClass(), LogLevel.class);
        logging(log, t, logLevel);
    }

    /**
     * Logs the specified exception with specified level.
     *
     * @param log      {@link Logger}
     * @param t        error to log
     * @param logLevel the level to logging. error level if {@code null} is specified.
     */
    public static void logging(@NonNull Logger log, @NonNull Throwable t, LogLevel logLevel) {
        if (logLevel == null) {
            log.error(t.getMessage(), t);
            return;
        }

        logging(log, t, logLevel.value());
    }

    /**
     * Logs the specified exception with specified level.
     *
     * @param log              {@link Logger}
     * @param t                error to log
     * @param informationLevel the level to logging. error level if {@code null} is specified.
     */
    public static void logging(@NonNull Logger log, @NonNull Throwable t, InformationLevel informationLevel) {
        if (informationLevel == null) {
            log.error(t.getMessage(), t);
            return;
        }

        switch (informationLevel) {
            case DEBUG:
                log.debug("[{}]{}", t.getClass().getSimpleName(), t.getMessage());
                return;
            case INFO:
                log.info("[{}]{}", t.getClass().getSimpleName(), t.getMessage());
                return;
            case WARN:
                log.warn(t.getMessage(), t);
                return;
            case ERROR:
                log.error(t.getMessage(), t);
                return;
        }
    }

}
