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

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import com.linecorp.clova.extension.boot.exception.UnsupportedHandlerArgumentException;
import com.linecorp.clova.extension.boot.handler.annnotation.CEKHandleCondition;
import com.linecorp.clova.extension.boot.handler.annnotation.CEKRequestHandler;
import com.linecorp.clova.extension.boot.handler.annnotation.CEKRequestMapping;
import com.linecorp.clova.extension.boot.handler.condition.CEKHandleConditionMatcher;
import com.linecorp.clova.extension.boot.handler.condition.CEKHandleConditionMatcherFactory;
import com.linecorp.clova.extension.boot.handler.resolver.CEKRequestHandlerArgumentResolver;
import com.linecorp.clova.extension.boot.message.request.RequestType;
import com.linecorp.clova.extension.boot.message.response.CEKResponse;
import com.linecorp.clova.extension.boot.util.StringUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * A class that holds each {@link RequestType}'s mapping of the Handler method.
 */
@Slf4j
@RequiredArgsConstructor
public class CEKRequestMappingHandlerMapping implements BeanFactoryAware, InitializingBean {

    private static final ParameterNameDiscoverer PARAMETER_NAME_DISCOVERER =
            new DefaultParameterNameDiscoverer();

    private final List<CEKRequestHandlerArgumentResolver> argumentResolvers;

    @Setter
    private BeanFactory beanFactory;

    @Getter
    private Map<RequestType, Map<CEKRequestKey, List<CEKHandlerMethod>>> handlerMethodMap;

    /**
     * Initializes the mapping of the Handler Methods.
     * <p>
     * Extracts Handler Methods from the annotated classes annotated of {@link CEKRequestHandler
     * &#64;CEKRequestHandler}.
     */
    @Override
    public void afterPropertiesSet() {
        ListableBeanFactory beanFactory = (ListableBeanFactory) this.beanFactory;
        Map<RequestType, Map<CEKRequestKey, List<CEKHandlerMethod>>> handlerMethodMap =
                beanFactory.getBeansWithAnnotation(CEKRequestHandler.class)
                           .values()
                           .stream()
                           .flatMap(requestHandler -> extractHandlerMethods(requestHandler).stream())
                           .collect(groupingBy(CEKHandlerMethod::getRequestType,
                                               groupingBy(CEKHandlerMethod::createKey,
                                                          mapping(identity(), toList()))));

        this.handlerMethodMap = Collections.unmodifiableMap(handlerMethodMap);
    }

    private List<CEKHandlerMethod> extractHandlerMethods(Object requestHandler) {
        Class<?> beanType = getOriginalBeanType(requestHandler);
        List<CEKHandlerMethod> handlerMethods = new ArrayList<>();

        Set<CEKHandleConditionMatcher> handlerConditionMatchers = conditionMatchers(beanType);

        ReflectionUtils.doWithMethods(beanType, method -> {
            CEKRequestMapping methodAnnotation =
                    AnnotatedElementUtils.getMergedAnnotation(method, CEKRequestMapping.class);
            if (methodAnnotation == null) {
                return;
            }
            String name = methodAnnotation.value();

            if (methodAnnotation.nameRequired() && StringUtils.isBlank(name)) {
                throw new IllegalStateException("Found invalid handler definition. ["
                                                + "name:" + name + ", "
                                                + "beanType:" + beanType + ", "
                                                + "method:" + method + "]");
            }

            MethodParameter returnType = new MethodParameter(method, -1);
            if (!CEKResponse.class.isAssignableFrom(returnType.getParameterType())) {
                throw new UnsupportedHandlerArgumentException(returnType, "Unsupported type method returns.");
            }

            List<MethodParameter> methodParams =
                    IntStream.range(0, method.getParameterCount())
                             .mapToObj(paramIndex -> new MethodParameter(method, paramIndex))
                             .peek(methodParam -> methodParam.initParameterNameDiscovery(
                                     PARAMETER_NAME_DISCOVERER))
                             .collect(toList());

            List<CEKRequestHandlerArgumentResolver> argumentResolvers =
                    methodParams.stream()
                                .map(this::extractArgumentResolver)
                                .collect(Collectors.toList());

            method.setAccessible(true);

            Set<CEKHandleConditionMatcher> methodConditionMatchers = conditionMatchers(method);

            CEKHandlerMethod handlerMethod = CEKHandlerMethod.builder()
                                                             .requestType(methodAnnotation.type())
                                                             .bean(requestHandler)
                                                             .method(method)
                                                             .name(name)
                                                             .methodParams(methodParams)
                                                             .argumentResolvers(argumentResolvers)
                                                             .handlerConditionMatchers(handlerConditionMatchers)
                                                             .methodConditionMatchers(methodConditionMatchers)
                                                             .build();

            handlerMethods.add(handlerMethod);

            log.info("Mapped {}", handlerMethod);
        });

        return handlerMethods;
    }

    private CEKRequestHandlerArgumentResolver extractArgumentResolver(MethodParameter methodParam) {
        return this.argumentResolvers.stream()
                                     .filter(argumentResolver -> argumentResolver.supports(methodParam))
                                     .findFirst()
                                     .orElseThrow(
                                             () -> new UnsupportedHandlerArgumentException(methodParam));
    }

    private Class<?> getOriginalBeanType(Object bean) {
        Class<?> beanType = ClassUtils.getUserClass(AopProxyUtils.ultimateTargetClass(bean));
        if (beanType.getSimpleName().contains("$MockitoMock$")) {
            try {
                beanType = Class.forName(beanType.getPackage().getName()
                                         + "." + beanType.getSimpleName().split("\\$MockitoMock\\$")[0]);
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException(e);
            }
        }
        return beanType;
    }

    @SuppressWarnings({ "unchecked", "rawTypes" })
    Set<CEKHandleConditionMatcher> conditionMatchers(Object beanTypeOrMethod) {
        Assert.isTrue(beanTypeOrMethod instanceof Class || beanTypeOrMethod instanceof Method,
                      "beanTypeOrMethod should be Class or Method. "
                      + "[beanTypeOrMethod:" + beanTypeOrMethod + "]");

        Set<CEKHandleConditionMatcher> matchers = new HashSet<>();
        for (Map.Entry<Class<? extends CEKHandleConditionMatcherFactory>, Set<Annotation>> entry
                : extractConditionMatcherFactories(beanTypeOrMethod).entrySet()) {
            CEKHandleConditionMatcher matcher = createHandleConditionMatcher(entry.getKey(), entry.getValue());
            matchers.add(matcher);
        }
        return matchers;
    }

    <F extends CEKHandleConditionMatcherFactory<M, A>,
            M extends CEKHandleConditionMatcher,
            A extends Annotation>
    M createHandleConditionMatcher(Class<F> factoryType, Collection<A> annotations) {
        F factory;
        try {
            factory = beanFactory.getBean(factoryType);
        } catch (NoSuchBeanDefinitionException e) {
            factory = BeanUtils.instantiateClass(factoryType);
        }
        return factory.create(annotations);
    }

    @SuppressWarnings("rawTypes")
    static Map<Class<? extends CEKHandleConditionMatcherFactory>, Set<Annotation>>
    extractConditionMatcherFactories(Object beanTypeOrMethod) {
        Assert.isTrue(beanTypeOrMethod instanceof Class || beanTypeOrMethod instanceof Method,
                      "beanTypeOrMethod should be Class or Method. "
                      + "[beanTypeOrMethod:" + beanTypeOrMethod + "]");

        Annotation[] annotations = AnnotationUtils.getAnnotations((AnnotatedElement) beanTypeOrMethod);

        if (annotations == null || annotations.length == 0) {
            return Collections.emptyMap();
        }

        return Arrays.stream(annotations)
                     .flatMap(annotation -> searchCEKHandleCondition(annotation).stream())
                     .collect(groupingBy(annotation -> annotation.annotationType()
                                                                 .getAnnotation(CEKHandleCondition.class)
                                                                 .value(),
                                         mapping(Function.identity(), Collectors.toSet())));
    }

    static Collection<Annotation> searchCEKHandleCondition(Annotation annotation) {
        Set<Annotation> annotations = new LinkedHashSet<>();
        if (annotation.annotationType().isAnnotationPresent(CEKHandleCondition.class)) {
            annotations.add(annotation);
            return annotations;
        }
        for (Annotation breakDownedAnnotation : breakDownIfRepeatable(annotation)) {
            if (breakDownedAnnotation.annotationType().isAnnotationPresent(CEKHandleCondition.class)) {
                annotations.add(breakDownedAnnotation);
            }
            Annotation[] metaAnnotations = AnnotationUtils.getAnnotations(
                    breakDownedAnnotation.annotationType());
            if (metaAnnotations == null || metaAnnotations.length == 0) {
                continue;
            }
            Arrays.stream(metaAnnotations)
                  .filter(metaAnnotation -> !metaAnnotation.annotationType().getName()
                                                           .startsWith("java.lang.annotation"))
                  .map(CEKRequestMappingHandlerMapping::searchCEKHandleCondition)
                  .forEach(annotations::addAll);
        }
        return annotations;
    }

    static Collection<Annotation> breakDownIfRepeatable(Annotation annotation) {
        Object value = AnnotationUtils.getValue(annotation);
        if (value == null) {
            return Collections.singleton(annotation);
        }
        if (!value.getClass().isArray()) {
            return Collections.singleton(annotation);
        }
        Object[] annotations = (Object[]) value;
        if (annotations.length == 0) {
            return Collections.singleton(annotation);
        }

        if (!Arrays.stream(annotations)
                   .findFirst()
                   .filter(Annotation.class::isInstance)
                   .map(Annotation.class::cast)
                   .map(repeating -> repeating.annotationType().getAnnotation(Repeatable.class))
                   .filter(repeatable -> repeatable.value().isInstance(annotation))
                   .isPresent()) {
            return Collections.singleton(annotation);
        }

        return Arrays.asList((Annotation[]) annotations);
    }

}
