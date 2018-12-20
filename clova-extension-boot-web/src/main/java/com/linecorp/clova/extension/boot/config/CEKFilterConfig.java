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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.AnnotatedTypeMetadata;

import com.linecorp.clova.extension.boot.filter.LoggingCEKResponseFilter;
import com.linecorp.clova.extension.boot.filter.logger.DefaultCEKResponseLogger;
import com.linecorp.clova.extension.boot.filter.logger.HttpServletResponseLogger;

/**
 * A configuration for {@link javax.servlet.Filter Filter}s.
 */
@Configuration
@EnableConfigurationProperties(CEKProperties.class)
public class CEKFilterConfig {

    @Bean
    @ConditionalOnBean(HttpServletResponseLogger.class)
    @ConditionalOnMissingBean
    LoggingCEKResponseFilter loggingCEKResponseFilter(List<HttpServletResponseLogger> loggers,
                                                      CEKProperties cekProperties) {
        return new LoggingCEKResponseFilter(loggers, cekProperties.getApiPath());
    }

    @Configuration
    static class CEKResponseLoggerConfig {

        @Bean
        @Conditional(CEKRequestResponseLoggerDebugEnableCondition.class)
        @ConditionalOnMissingBean
        DefaultCEKResponseLogger defaultCEKResponseLogger() {
            return new DefaultCEKResponseLogger();
        }

        static class CEKRequestResponseLoggerDebugEnableCondition implements Condition {

            private static final Logger CEK_RESPONSE_MESSAGE_LOGGER =
                    LoggerFactory.getLogger("cek.message.response");

            @Override
            public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
                return CEK_RESPONSE_MESSAGE_LOGGER.isDebugEnabled();
            }

        }

    }

}
