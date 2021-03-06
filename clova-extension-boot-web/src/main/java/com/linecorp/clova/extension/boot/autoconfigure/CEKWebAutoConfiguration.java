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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
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
            ObjectProvider<SmartValidator> validatorProvider) {
        return new CEKResponseMessageValidationAdvice(validatorProvider.getObject());
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
            ObjectProvider<ObjectMapper> objectMapperProvider,
            CEKProperties cekProperties) {
        return new CEKHandleIntentController(requestProcessor, objectMapperProvider.getObject(),
                                             cekProperties.getClient().getDefaultLocale());
    }

    @Bean
    CEKRequestHandlerDispatcher cekRequestHandlerDispatcher(
            CEKRequestMappingHandlerMapping handlerMapping,
            ObjectProvider<SmartValidator> validatorProvider,
            ObjectProvider<ObjectMapper> objectMapperProvider,
            ObjectProvider<List<CEKRequestVerifier>> requestVerifiers,
            ObjectProvider<Map<String, CEKHandlerInterceptor>> handlerInterceptorMap) {
        CEKRequestHandlerDispatcher dispatcher =
                new CEKRequestHandlerDispatcher(handlerMapping, validatorProvider.getObject(),
                                                objectMapperProvider.getObject());

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

    @SuppressWarnings("unchecked")
    Map<String, CEKHandlerInterceptor> sortByOrder(Map<String, CEKHandlerInterceptor> handlerInterceptorMap) {
        return handlerInterceptorMap.entrySet().stream()
                                    .sorted(AnnotationAwareOrderComparator.INSTANCE.withSourceProvider(
                                            entry -> ((Map.Entry<String, CEKHandlerInterceptor>) entry)
                                                    .getValue()))
                                    .collect(Collectors.toMap(Map.Entry::getKey,
                                                              Map.Entry::getValue,
                                                              (v1, v2) -> v1,
                                                              LinkedHashMap::new));
    }

    @Configuration
    static class ArgumentResolverConfig {

        @Bean
        CEKContextPropertyArgumentResolver cekContextPropertyArgumentResolver(
                ObjectProvider<ObjectMapper> objectMapperProvider) {
            return new CEKContextPropertyArgumentResolver(objectMapperProvider.getObject());
        }

        @Bean
        CEKEventPayloadArgumentResolver cekEventPayloadArgumentResolver(
                ObjectProvider<ObjectMapper> objectMapperProvider) {
            return new CEKEventPayloadArgumentResolver(objectMapperProvider.getObject());
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
        CEKSessionHolderArgumentResolver cekSessionHolderArgumentResolver(
                ObjectProvider<ObjectMapper> objectMapperProvider) {
            return new CEKSessionHolderArgumentResolver(objectMapperProvider.getObject());
        }

        @Bean
        CEKSessionValueArgumentResolver cekSessionValueArgumentResolver(
                ObjectProvider<ObjectMapper> objectMapperProvider) {
            return new CEKSessionValueArgumentResolver(objectMapperProvider.getObject());
        }

        @Bean
        CEKSlotValueArgumentResolver cekSlotValueArgumentResolver(
                ObjectProvider<ObjectMapper> objectMapperProvider) {
            return new CEKSlotValueArgumentResolver(objectMapperProvider.getObject());
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
