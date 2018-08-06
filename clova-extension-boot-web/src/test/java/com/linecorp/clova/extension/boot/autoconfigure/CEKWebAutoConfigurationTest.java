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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import com.linecorp.clova.extension.boot.handler.interceptor.CEKHandlerInterceptor;

public class CEKWebAutoConfigurationTest {

    CEKWebAutoConfiguration config = new CEKWebAutoConfiguration();

    @Test
    public void sortByOrder() throws Exception {
        Map<String, CEKHandlerInterceptor> handlerInterceptorMap = new HashMap<>();

        handlerInterceptorMap.put("a", new InterceptorA());
        handlerInterceptorMap.put("b", new InterceptorB());
        handlerInterceptorMap.put("c", new InterceptorC());
        handlerInterceptorMap.put("d", new InterceptorD());
        handlerInterceptorMap.put("e", new InterceptorE());

        Map<String, CEKHandlerInterceptor> actual = config.sortByOrder(handlerInterceptorMap);

        assertThat(actual.entrySet())
                .hasSameSizeAs(handlerInterceptorMap.entrySet())
                .extracting(Map.Entry::getKey)
                .containsSequence("e", "d", "b", "a", "c");
    }

    static class InterceptorA implements CEKHandlerInterceptor, Ordered {

        @Override
        public int getOrder() {
            return 1;
        }
    }

    @Order(0)
    static class InterceptorB implements CEKHandlerInterceptor {}

    static class InterceptorC implements CEKHandlerInterceptor {}

    static class InterceptorD implements CEKHandlerInterceptor, Ordered {

        @Override
        public int getOrder() {
            return -1;
        }
    }

    @Order(-2)
    static class InterceptorE implements CEKHandlerInterceptor {}

}
