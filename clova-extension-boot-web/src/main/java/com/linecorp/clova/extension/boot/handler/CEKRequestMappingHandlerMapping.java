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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import com.linecorp.clova.extension.boot.exception.UnsupportedHandlerArgumentException;
import com.linecorp.clova.extension.boot.handler.annnotation.CEKRequestHandler;
import com.linecorp.clova.extension.boot.handler.annnotation.CEKRequestMapping;
import com.linecorp.clova.extension.boot.handler.condition.CEKHandleConditionMatcher;
import com.linecorp.clova.extension.boot.message.request.RequestType;
import com.linecorp.clova.extension.boot.message.response.CEKResponse;
import com.linecorp.clova.extension.boot.util.StringUtils;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * A class that holds each {@link RequestType}'s mapping of the Handler method.
 */
@Slf4j
public class CEKRequestMappingHandlerMapping implements BeanFactoryAware, InitializingBean {

    private static final ParameterNameDiscoverer PARAMETER_NAME_DISCOVERER =
            new DefaultParameterNameDiscoverer();

    @Setter
    private BeanFactory beanFactory;

    @Getter
    private Map<RequestType, Map<CEKRequestKey, List<CEKHandlerMethod>>> handlerMethodMap;

    /**
     * Initializes the mapping of the Handler Methods.
     * <p>
     * Extracts Handler Methods from the annotated classes annotated of {@link CEKRequestHandler @CEKRequestHandler}.
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

            MethodParameter returnType = MethodParameter.forExecutable(method, -1);
            if (!CEKResponse.class.isAssignableFrom(returnType.getParameterType())) {
                throw new UnsupportedHandlerArgumentException(returnType, "Unsupported type method returns.");
            }

            List<MethodParameter> methodParams =
                    IntStream.range(0, method.getParameterCount())
                             .mapToObj(paramIndex -> MethodParameter.forExecutable(method, paramIndex))
                             .peek(methodParam -> methodParam.initParameterNameDiscovery(
                                     PARAMETER_NAME_DISCOVERER))
                             .collect(toList());

            method.setAccessible(true);

            CEKHandlerMethod handlerMethod = CEKHandlerMethod.builder()
                                                             .requestType(methodAnnotation.type())
                                                             .bean(requestHandler)
                                                             .method(method)
                                                             .name(name)
                                                             .methodParams(methodParams)
                                                             .conditionMatchers(conditionMatchers(method))
                                                             .build();

            handlerMethods.add(handlerMethod);

            log.info("Mapped {}", handlerMethod);
        });

        return handlerMethods;
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

    protected Set<CEKHandleConditionMatcher> conditionMatchers(Method method) {
        return Collections.emptySet();
    }

}
