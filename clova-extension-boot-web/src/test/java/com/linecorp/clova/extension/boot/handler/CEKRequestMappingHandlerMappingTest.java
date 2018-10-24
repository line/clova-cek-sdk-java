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

package com.linecorp.clova.extension.boot.handler;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.ReflectionUtils;

import com.linecorp.clova.extension.boot.handler.CEKRequestMappingHandlerMappingTest.TestConfig.TestHandler;
import com.linecorp.clova.extension.boot.handler.annnotation.CEKHandleCondition;
import com.linecorp.clova.extension.boot.handler.annnotation.CEKRequestHandler;
import com.linecorp.clova.extension.boot.handler.annnotation.ExtensionIdCondition;
import com.linecorp.clova.extension.boot.handler.annnotation.LaunchMapping;
import com.linecorp.clova.extension.boot.handler.condition.CEKHandleConditionMatcher;
import com.linecorp.clova.extension.boot.handler.condition.CEKHandleConditionMatcherFactory;
import com.linecorp.clova.extension.boot.handler.condition.ExtensionIdConditionMatcher;
import com.linecorp.clova.extension.boot.handler.condition.ExtensionIdConditionMatcherFactory;
import com.linecorp.clova.extension.boot.message.context.SystemContext;
import com.linecorp.clova.extension.boot.message.request.CEKRequestMessage;
import com.linecorp.clova.extension.boot.message.response.CEKResponse;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RunWith(SpringRunner.class)
@ActiveProfiles("CEKRequestMappingHandlerMappingTest")
public class CEKRequestMappingHandlerMappingTest {

    @Configuration
    @Profile("CEKRequestMappingHandlerMappingTest")
    static class TestConfig {

        @Bean
        CEKRequestMappingHandlerMapping cekRequestMappingHandlerMapping() {
            return new CEKRequestMappingHandlerMapping();
        }

        @Bean
        DummyBean dummyBean() {
            return new DummyBean();
        }

        @Bean
        DummyConditionMatcherFactory dummyConditionMatcherFactory() {
            return new DummyConditionMatcherFactory(dummyBean());
        }

        @CEKRequestHandler
        @ExtensionIdCondition("foo")
        @Profile("CEKRequestMappingHandlerMappingTest")
        static class TestHandler {

            @LaunchMapping
            @DummyCondition("dummy1")
            @DummyCondition("dummy2")
            @DummyAnnotation
            CEKResponse testMethod() {
                return CEKResponse.empty();
            }

            static Method getTestMethod() {
                return ReflectionUtils.findMethod(TestHandler.class, "testMethod");
            }
        }

    }

    @Autowired
    CEKRequestMappingHandlerMapping cekRequestMappingHandlerMapping;
    @Autowired
    DummyBean dummyBean;

    @Test
    public void conditionMatchers_Method() throws Exception {
        Set<CEKHandleConditionMatcher> matchers =
                cekRequestMappingHandlerMapping.conditionMatchers(TestHandler.getTestMethod());
        assertThat(matchers)
                .hasSize(1)
                .anySatisfy(matcher -> assertThat(matcher).isInstanceOf(DummyConditionMatcher.class));
    }

    @Test
    public void conditionMatchers_BeanType() throws Exception {
        Set<CEKHandleConditionMatcher> matchers =
                cekRequestMappingHandlerMapping.conditionMatchers(TestHandler.class);
        assertThat(matchers)
                .hasSize(1)
                .anySatisfy(matcher -> assertThat(matcher).isInstanceOf(ExtensionIdConditionMatcher.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void createHandleConditionMatcher_Method_SingletonFactory() throws Exception {
        Set<DummyCondition> annotations = AnnotationUtils.getRepeatableAnnotations(
                TestHandler.getTestMethod(), DummyCondition.class);
        DummyConditionMatcher matcher = cekRequestMappingHandlerMapping.createHandleConditionMatcher(
                DummyConditionMatcherFactory.class, annotations);
        assertThat(matcher.getAnnotations()).isSameAs(annotations);

        DummyBean dummyBean = (DummyBean) ReflectionTestUtils.getField(matcher, "dummyBean");
        assertThat(dummyBean).isSameAs(this.dummyBean);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void createHandleConditionMatcher_BeanType_NonSingleton() throws Exception {
        Set<ExtensionIdCondition> annotations = AnnotationUtils.getRepeatableAnnotations(
                TestHandler.class, ExtensionIdCondition.class);
        ExtensionIdConditionMatcher matcher = cekRequestMappingHandlerMapping.createHandleConditionMatcher(
                ExtensionIdConditionMatcherFactory.class, annotations);
        Collection<String> extensionIds =
                (Collection<String>) ReflectionTestUtils.getField(matcher, "extensionIds");
        assertThat(extensionIds).containsOnlyOnce("foo");
    }

    @Test
    public void extractConditionMatcherFactories_Method() throws Exception {
        Method method = TestHandler.getTestMethod();
        Map<Class<? extends CEKHandleConditionMatcherFactory>, Set<Annotation>> conditionMatcherFactories =
                CEKRequestMappingHandlerMapping.extractConditionMatcherFactories(method);

        assertThat(conditionMatcherFactories)
                .containsOnlyKeys(DummyConditionMatcherFactory.class);

        assertThat(conditionMatcherFactories.get(DummyConditionMatcherFactory.class))
                .isEqualTo(AnnotationUtils.getRepeatableAnnotations(method, DummyCondition.class));

        assertThat(conditionMatcherFactories.get(DummyConditionMatcherFactory.class))
                .isEqualTo(AnnotatedElementUtils.getMergedRepeatableAnnotations(method, DummyCondition.class));
    }

    @Test
    public void extractConditionMatcherFactories_BeanType() throws Exception {
        Map<Class<? extends CEKHandleConditionMatcherFactory>, Set<Annotation>> conditionMatcherFactories =
                CEKRequestMappingHandlerMapping.extractConditionMatcherFactories(TestHandler.class);

        assertThat(conditionMatcherFactories)
                .containsOnlyKeys(ExtensionIdConditionMatcherFactory.class);

        assertThat(conditionMatcherFactories.get(ExtensionIdConditionMatcherFactory.class))
                .containsOnlyOnce(AnnotationUtils.getAnnotation(TestHandler.class, ExtensionIdCondition.class))
                .hasSize(1);
    }

    @Test
    public void breakDownIfRepeatable() throws Exception {
        Method method = TestHandler.getTestMethod();
        Collection<Annotation> actual =
                CEKRequestMappingHandlerMapping.breakDownIfRepeatable(
                        method.getAnnotation(DummyConditions.class));

        List<Annotation> expected = Arrays.asList(method.getAnnotationsByType(DummyCondition.class));

        assertThat(actual).isEqualTo(expected);
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface DummyAnnotation {}

    @Target({ ElementType.METHOD, ElementType.TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @CEKHandleCondition(DummyConditionMatcherFactory.class)
    @Repeatable(DummyConditions.class)
    public @interface DummyCondition {
        String value();
    }

    @Target({ ElementType.METHOD, ElementType.TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface DummyConditions {
        DummyCondition[] value();
    }

    @RequiredArgsConstructor
    static class DummyConditionMatcher implements CEKHandleConditionMatcher {

        final DummyBean dummyBean;
        @Getter
        final Collection<DummyCondition> annotations;

        @Override
        public boolean match(HttpServletRequest request, CEKRequestMessage requestMessage,
                             SystemContext system) {
            return false;
        }
    }

    @RequiredArgsConstructor
    static class DummyConditionMatcherFactory
            implements CEKHandleConditionMatcherFactory<DummyConditionMatcher, DummyCondition> {

        final DummyBean dummyBean;

        @Override
        public DummyConditionMatcher create(Collection<DummyCondition> annotations) {
            return new DummyConditionMatcher(dummyBean, annotations);
        }
    }

    static class DummyBean {}

}
