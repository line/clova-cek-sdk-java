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

package com.linecorp.clova.extension.boot.autoconfigure;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.util.ClassUtils;
import org.springframework.validation.SmartValidator;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.linecorp.clova.extension.boot.config.CEKFilterConfig;
import com.linecorp.clova.extension.boot.config.CEKProperties;
import com.linecorp.clova.extension.boot.config.CEKRequestVerificationConfig;
import com.linecorp.clova.extension.boot.controller.CEKHandleIntentController;
import com.linecorp.clova.extension.boot.controller.advice.CEKHandleIntentControllerAdvice;
import com.linecorp.clova.extension.boot.controller.advice.CEKResponseMessageValidationAdvice;
import com.linecorp.clova.extension.boot.controller.advice.CEKSDKInformationBinderResponseBodyAdvice;
import com.linecorp.clova.extension.boot.handler.CEKRequestHandlerDispatcher;
import com.linecorp.clova.extension.boot.handler.CEKRequestMappingHandlerMapping;
import com.linecorp.clova.extension.boot.handler.CEKRequestProcessor;
import com.linecorp.clova.extension.boot.handler.interceptor.CEKHandlerInterceptor;
import com.linecorp.clova.extension.boot.handler.resolver.CEKContextPropertyArgumentResolver;
import com.linecorp.clova.extension.boot.handler.resolver.CEKEventPayloadArgumentResolver;
import com.linecorp.clova.extension.boot.handler.resolver.CEKEventRequestArgumentResolver;
import com.linecorp.clova.extension.boot.handler.resolver.CEKIntentRequestArgumentResolver;
import com.linecorp.clova.extension.boot.handler.resolver.CEKRequestHandlerArgumentResolver;
import com.linecorp.clova.extension.boot.handler.resolver.CEKRequestTypeArgumentResolver;
import com.linecorp.clova.extension.boot.handler.resolver.CEKSessionArgumentResolver;
import com.linecorp.clova.extension.boot.handler.resolver.CEKSessionHolderArgumentResolver;
import com.linecorp.clova.extension.boot.handler.resolver.CEKSessionValueArgumentResolver;
import com.linecorp.clova.extension.boot.handler.resolver.CEKSlotValueArgumentResolver;
import com.linecorp.clova.extension.boot.handler.resolver.HttpServletRequestArgumentResolver;
import com.linecorp.clova.extension.boot.handler.resolver.HttpServletResponseArgumentResolver;
import com.linecorp.clova.extension.boot.message.speech.OutputSpeechGenerator;
import com.linecorp.clova.extension.boot.verifier.CEKRequestVerifier;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Clova Extension Boot.
 */
@Configuration
@Import({CEKRequestVerificationConfig.class, CEKFilterConfig.class})
@EnableConfigurationProperties(CEKProperties.class)
public class CEKWebAutoConfiguration {

    @Bean
    CEKResponseMessageValidationAdvice cekResponseMessageValidationAdvice(
            @SuppressWarnings("SpringJavaAutowiringInspection") SmartValidator validator) {
        return new CEKResponseMessageValidationAdvice(validator);
    }

    @Bean
    CEKSDKInformationBinderResponseBodyAdvice cekSDKInformationBinderResponseBodyAdvice() {
        return new CEKSDKInformationBinderResponseBodyAdvice();
    }

    @Bean
    CEKHandleIntentControllerAdvice cekHandleIntentControllerAdvice(
            OutputSpeechGenerator outputSpeechGenerator) {
        return new CEKHandleIntentControllerAdvice(outputSpeechGenerator);
    }

    @Bean
    @ConditionalOnMissingBean(CEKHandleIntentController.class)
    CEKHandleIntentController cekHandleIntentController(
            CEKRequestProcessor requestProcessor,
            @SuppressWarnings("SpringJavaAutowiringInspection") ObjectMapper objectMapper,
            CEKProperties cekProperties) {
        return new CEKHandleIntentController(requestProcessor, objectMapper,
                                             cekProperties.getClient().getDefaultLocale());
    }

    @Bean
    CEKRequestHandlerDispatcher cekRequestHandlerDispatcher(
            CEKRequestMappingHandlerMapping handlerMapping,
            @SuppressWarnings("SpringJavaAutowiringInspection") SmartValidator validator,
            @SuppressWarnings("SpringJavaAutowiringInspection") ObjectMapper objectMapper,
            ObjectProvider<List<CEKRequestVerifier>> requestVerifiers,
            ObjectProvider<Map<String, CEKHandlerInterceptor>> handlerInterceptorMap) {
        CEKRequestHandlerDispatcher dispatcher =
                new CEKRequestHandlerDispatcher(handlerMapping, validator, objectMapper);

        Optional.ofNullable(requestVerifiers.getIfAvailable())
                .filter(list -> !list.isEmpty())
                .ifPresent(dispatcher::setRequestVerifiers);
        Optional.ofNullable(handlerInterceptorMap.getIfAvailable())
                .filter(map -> !map.isEmpty())
                .map(this::sortByOrder)
                .ifPresent(dispatcher::setHandlerInterceptorMap);

        return dispatcher;
    }

    @Bean
    CEKRequestMappingHandlerMapping cekRequestMappingHandlerMapping(
            List<CEKRequestHandlerArgumentResolver> argumentResolvers) {
        return new CEKRequestMappingHandlerMapping(argumentResolvers);
    }

    @Bean
    OutputSpeechGenerator outputSpeechGenerator(MessageSource messageSource) {
        return new OutputSpeechGenerator(messageSource);
    }

    Map<String, CEKHandlerInterceptor> sortByOrder(Map<String, CEKHandlerInterceptor> handlerInterceptorMap) {
        return handlerInterceptorMap.entrySet().stream()
                                    .sorted(Comparator.comparing(entry -> getOrder(entry.getValue())))
                                    .collect(Collectors.toMap(Map.Entry::getKey,
                                                              Map.Entry::getValue,
                                                              (v1, v2) -> v1,
                                                              LinkedHashMap::new));
    }

    private static int getOrder(Object obj) {
        if (obj == null) {
            return Ordered.LOWEST_PRECEDENCE;
        }
        Class<?> beanType = ClassUtils.getUserClass(AopProxyUtils.ultimateTargetClass(obj));
        if (Ordered.class.isAssignableFrom(beanType)) {
            return ((Ordered) obj).getOrder();
        }

        return (int) Optional.ofNullable(AnnotatedElementUtils.getMergedAnnotation(beanType, Order.class))
                             .map(AnnotationUtils::getValue)
                             .orElse(Ordered.LOWEST_PRECEDENCE);
    }

    @Configuration
    static class ArgumentResolverConfig {

        @Bean
        CEKContextPropertyArgumentResolver cekContextPropertyArgumentResolver(ObjectMapper objectMapper) {
            return new CEKContextPropertyArgumentResolver(objectMapper);
        }

        @Bean
        CEKEventPayloadArgumentResolver cekEventPayloadArgumentResolver(ObjectMapper objectMapper) {
            return new CEKEventPayloadArgumentResolver(objectMapper);
        }

        @Bean
        CEKEventRequestArgumentResolver cekEventRequestArgumentResolver() {
            return new CEKEventRequestArgumentResolver();
        }

        @Bean
        CEKIntentRequestArgumentResolver cekIntentRequestArgumentResolver() {
            return new CEKIntentRequestArgumentResolver();
        }

        @Bean
        CEKRequestTypeArgumentResolver cekRequestTypeArgumentResolver() {
            return new CEKRequestTypeArgumentResolver();
        }

        @Bean
        CEKSessionArgumentResolver cekSessionArgumentResolver() {
            return new CEKSessionArgumentResolver();
        }

        @Bean
        CEKSessionHolderArgumentResolver cekSessionHolderArgumentResolver(ObjectMapper objectMapper) {
            return new CEKSessionHolderArgumentResolver(objectMapper);
        }

        @Bean
        CEKSessionValueArgumentResolver cekSessionValueArgumentResolver(ObjectMapper objectMapper) {
            return new CEKSessionValueArgumentResolver(objectMapper);
        }

        @Bean
        CEKSlotValueArgumentResolver cekSlotValueArgumentResolver(ObjectMapper objectMapper) {
            return new CEKSlotValueArgumentResolver(objectMapper);
        }

        @Bean
        HttpServletRequestArgumentResolver httpServletRequestArgumentResolver() {
            return new HttpServletRequestArgumentResolver();
        }

        @Bean
        HttpServletResponseArgumentResolver httpServletResponseArgumentResolver() {
            return new HttpServletResponseArgumentResolver();
        }

    }

}
