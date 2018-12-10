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
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.verification.VerificationMode;
import org.springframework.core.MethodParameter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.ReflectionUtils;

import com.linecorp.clova.extension.boot.exception.CEKHandlerInterceptException;
import com.linecorp.clova.extension.boot.handler.interceptor.CEKHandlerInterceptor;
import com.linecorp.clova.extension.boot.message.request.CEKRequestMessage;
import com.linecorp.clova.extension.boot.message.request.RequestType;
import com.linecorp.clova.extension.boot.message.response.CEKResponse;
import com.linecorp.clova.extension.boot.message.response.CEKResponseMessage;
import com.linecorp.clova.extension.boot.session.SessionHolder;

import lombok.Data;
import lombok.SneakyThrows;

public class CEKRequestHandlerDispatcherInterceptorTest {

    MockSet mockSet;

    @Before
    public void setUp() {
        this.mockSet = new MockSet();
        ReflectionTestUtils.setField(
                mockSet.dispatcher,
                "handlerInterceptorMap",
                mockSet.handlerInterceptors
                        .stream()
                        .collect(toMap(interceptor -> "interceptor."
                                                      + mockSet.handlerInterceptors.indexOf(interceptor),
                                       identity())));
    }

    @Test
    @SneakyThrows
    public void invokeWithInterceptors_neverThrows() {
        mockSet.doReturn();

        mockSet.dispatcher.invokeWithInterceptors(mockSet.request, mockSet.requestMessage,
                                                  mockSet.sessionHolder,
                                                  mockSet.handlerMethod, mockSet.args);

        verify(mockSet.handlerInterceptor1, once())
                .shouldNotIntercept(eq(mockSet.request), eq(mockSet.requestMessage), eq(mockSet.handlerMethod),
                                    eq(mockSet.args));
        verify(mockSet.handlerInterceptor2, once())
                .shouldNotIntercept(eq(mockSet.request), eq(mockSet.requestMessage), eq(mockSet.handlerMethod),
                                    eq(mockSet.args));

        verify(mockSet.handlerInterceptor1, once())
                .preHandle(eq(mockSet.request), eq(mockSet.requestMessage), eq(mockSet.handlerMethod),
                           eq(mockSet.args));
        verify(mockSet.handlerInterceptor2, once())
                .preHandle(eq(mockSet.request), eq(mockSet.requestMessage), eq(mockSet.handlerMethod),
                           eq(mockSet.args));

        verify(mockSet.handlerInterceptor1, once())
                .postHandle(eq(mockSet.request), eq(mockSet.requestMessage), eq(mockSet.responseMessage),
                            eq(mockSet.handlerMethod), eq(mockSet.args));
        verify(mockSet.handlerInterceptor2, once())
                .postHandle(eq(mockSet.request), eq(mockSet.requestMessage), eq(mockSet.responseMessage),
                            eq(mockSet.handlerMethod), eq(mockSet.args));
    }

    @Test
    @SneakyThrows
    public void invokeWithInterceptors_handlerInterceptor1_shouldNotIntercept_throws_CEKHandlerInterceptException() {
        mockSet.doReturn();

        CEKHandlerInterceptException exception = new CEKHandlerInterceptException(mockSet.handlerMethod,
                                                                                  mockSet.args,
                                                                                  new Exception());

        when(mockSet.handlerInterceptor1.shouldNotIntercept(eq(mockSet.request), eq(mockSet.requestMessage),
                                                            eq(mockSet.handlerMethod), eq(mockSet.args)))
                .thenThrow(exception);

        assertThatThrownBy(() -> mockSet.dispatcher.invokeWithInterceptors(
                mockSet.request, mockSet.requestMessage, mockSet.sessionHolder, mockSet.handlerMethod,
                mockSet.args))
                .isEqualTo(exception);

        verify(mockSet.handlerInterceptor1, once())
                .shouldNotIntercept(eq(mockSet.request), eq(mockSet.requestMessage), eq(mockSet.handlerMethod),
                                    eq(mockSet.args));
        verify(mockSet.handlerInterceptor2, never())
                .shouldNotIntercept(eq(mockSet.request), eq(mockSet.requestMessage), eq(mockSet.handlerMethod),
                                    eq(mockSet.args));

        verify(mockSet.handlerInterceptor1, never())
                .preHandle(eq(mockSet.request), eq(mockSet.requestMessage), eq(mockSet.handlerMethod),
                           eq(mockSet.args));
        verify(mockSet.handlerInterceptor2, never())
                .preHandle(eq(mockSet.request), eq(mockSet.requestMessage), eq(mockSet.handlerMethod),
                           eq(mockSet.args));

        verify(mockSet.handlerInterceptor1, never())
                .postHandle(eq(mockSet.request), eq(mockSet.requestMessage), isNull(),
                            eq(mockSet.handlerMethod), eq(mockSet.args));
        verify(mockSet.handlerInterceptor2, never())
                .postHandle(eq(mockSet.request), eq(mockSet.requestMessage), isNull(),
                            eq(mockSet.handlerMethod), eq(mockSet.args));
    }

    @Test
    @SneakyThrows
    public void invokeWithInterceptors_handlerInterceptor1_shouldNotIntercept_throws_Exception() {
        mockSet.doReturn();

        RuntimeException cause = new RuntimeException();

        when(mockSet.handlerInterceptor1.shouldNotIntercept(eq(mockSet.request), eq(mockSet.requestMessage),
                                                            eq(mockSet.handlerMethod), eq(mockSet.args)))
                .thenThrow(cause);

        assertThatThrownBy(() -> mockSet.dispatcher.invokeWithInterceptors(
                mockSet.request, mockSet.requestMessage, mockSet.sessionHolder, mockSet.handlerMethod,
                mockSet.args))
                .isEqualTo(cause);

        verify(mockSet.handlerInterceptor1, once())
                .shouldNotIntercept(eq(mockSet.request), eq(mockSet.requestMessage), eq(mockSet.handlerMethod),
                                    eq(mockSet.args));
        verify(mockSet.handlerInterceptor2, never())
                .shouldNotIntercept(eq(mockSet.request), eq(mockSet.requestMessage), eq(mockSet.handlerMethod),
                                    eq(mockSet.args));

        verify(mockSet.handlerInterceptor1, never())
                .preHandle(eq(mockSet.request), eq(mockSet.requestMessage), eq(mockSet.handlerMethod),
                           eq(mockSet.args));
        verify(mockSet.handlerInterceptor2, never())
                .preHandle(eq(mockSet.request), eq(mockSet.requestMessage), eq(mockSet.handlerMethod),
                           eq(mockSet.args));

        verify(mockSet.handlerInterceptor1, never())
                .postHandle(eq(mockSet.request), eq(mockSet.requestMessage), isNull(),
                            eq(mockSet.handlerMethod), eq(mockSet.args));
        verify(mockSet.handlerInterceptor2, never())
                .postHandle(eq(mockSet.request), eq(mockSet.requestMessage), isNull(),
                            eq(mockSet.handlerMethod), eq(mockSet.args));
    }

    @Test
    @SneakyThrows
    public void invokeWithInterceptors_handlerInterceptor1_preHandle_throws_CEKHandlerInterceptException() {
        mockSet.doReturn();

        CEKHandlerInterceptException exception = new CEKHandlerInterceptException(mockSet.handlerMethod,
                                                                                  mockSet.args,
                                                                                  new Exception());

        doThrow(exception).when(mockSet.handlerInterceptor1).preHandle(eq(mockSet.request),
                                                                       eq(mockSet.requestMessage),
                                                                       eq(mockSet.handlerMethod),
                                                                       eq(mockSet.args));

        assertThatThrownBy(() -> mockSet.dispatcher.invokeWithInterceptors(
                mockSet.request, mockSet.requestMessage, mockSet.sessionHolder, mockSet.handlerMethod,
                mockSet.args))
                .isEqualTo(exception);

        verify(mockSet.handlerInterceptor1, once())
                .shouldNotIntercept(eq(mockSet.request), eq(mockSet.requestMessage), eq(mockSet.handlerMethod),
                                    eq(mockSet.args));
        verify(mockSet.handlerInterceptor2, once())
                .shouldNotIntercept(eq(mockSet.request), eq(mockSet.requestMessage), eq(mockSet.handlerMethod),
                                    eq(mockSet.args));

        verify(mockSet.handlerInterceptor1, once())
                .preHandle(eq(mockSet.request), eq(mockSet.requestMessage), eq(mockSet.handlerMethod),
                           eq(mockSet.args));
        verify(mockSet.handlerInterceptor2, never())
                .preHandle(eq(mockSet.request), eq(mockSet.requestMessage), eq(mockSet.handlerMethod),
                           eq(mockSet.args));

        verify(mockSet.handlerInterceptor1, once())
                .postHandle(eq(mockSet.request), eq(mockSet.requestMessage), isNull(),
                            eq(mockSet.handlerMethod), eq(mockSet.args));
        verify(mockSet.handlerInterceptor2, never())
                .postHandle(eq(mockSet.request), eq(mockSet.requestMessage), isNull(),
                            eq(mockSet.handlerMethod), eq(mockSet.args));
    }

    @Test
    @SneakyThrows
    public void invokeWithInterceptors_handlerInterceptor1_preHanle_throws_Exception() {
        mockSet.doReturn();

        RuntimeException cause = new RuntimeException();
        CEKHandlerInterceptException exception = new CEKHandlerInterceptException(mockSet.handlerMethod,
                                                                                  mockSet.args, cause);

        doThrow(cause).when(mockSet.handlerInterceptor1).preHandle(eq(mockSet.request),
                                                                   eq(mockSet.requestMessage),
                                                                   eq(mockSet.handlerMethod), eq(mockSet.args));

        assertThatThrownBy(() -> mockSet.dispatcher.invokeWithInterceptors(
                mockSet.request, mockSet.requestMessage, mockSet.sessionHolder, mockSet.handlerMethod,
                mockSet.args))
                .isExactlyInstanceOf(CEKHandlerInterceptException.class)
                .hasCause(cause)
                .isEqualToComparingOnlyGivenFields(exception, "handlerMethod", "args");

        verify(mockSet.handlerInterceptor1, once())
                .shouldNotIntercept(eq(mockSet.request), eq(mockSet.requestMessage), eq(mockSet.handlerMethod),
                                    eq(mockSet.args));
        verify(mockSet.handlerInterceptor2, once())
                .shouldNotIntercept(eq(mockSet.request), eq(mockSet.requestMessage), eq(mockSet.handlerMethod),
                                    eq(mockSet.args));

        verify(mockSet.handlerInterceptor1, once())
                .preHandle(eq(mockSet.request), eq(mockSet.requestMessage), eq(mockSet.handlerMethod),
                           eq(mockSet.args));
        verify(mockSet.handlerInterceptor2, never())
                .preHandle(eq(mockSet.request), eq(mockSet.requestMessage), eq(mockSet.handlerMethod),
                           eq(mockSet.args));

        verify(mockSet.handlerInterceptor1, once())
                .postHandle(eq(mockSet.request), eq(mockSet.requestMessage), isNull(),
                            eq(mockSet.handlerMethod), eq(mockSet.args));
        verify(mockSet.handlerInterceptor2, never())
                .postHandle(eq(mockSet.request), eq(mockSet.requestMessage), isNull(),
                            eq(mockSet.handlerMethod), eq(mockSet.args));
    }

    @Test
    @SneakyThrows
    public void invokeWithInterceptors_handlerInterceptor1_postHandle_throws_CEKHandlerInterceptException() {
        mockSet.doReturn();

        CEKHandlerInterceptException exception = new CEKHandlerInterceptException(mockSet.handlerMethod,
                                                                                  mockSet.args,
                                                                                  new Exception());

        doThrow(exception).when(mockSet.handlerInterceptor1).postHandle(eq(mockSet.request),
                                                                        eq(mockSet.requestMessage),
                                                                        eq(mockSet.responseMessage),
                                                                        eq(mockSet.handlerMethod),
                                                                        eq(mockSet.args));

        assertThatThrownBy(() -> mockSet.dispatcher.invokeWithInterceptors(
                mockSet.request, mockSet.requestMessage, mockSet.sessionHolder, mockSet.handlerMethod,
                mockSet.args))
                .isEqualTo(exception);

        verify(mockSet.handlerInterceptor1, once())
                .shouldNotIntercept(eq(mockSet.request), eq(mockSet.requestMessage), eq(mockSet.handlerMethod),
                                    eq(mockSet.args));
        verify(mockSet.handlerInterceptor2, once())
                .shouldNotIntercept(eq(mockSet.request), eq(mockSet.requestMessage), eq(mockSet.handlerMethod),
                                    eq(mockSet.args));

        verify(mockSet.handlerInterceptor1, once())
                .preHandle(eq(mockSet.request), eq(mockSet.requestMessage), eq(mockSet.handlerMethod),
                           eq(mockSet.args));
        verify(mockSet.handlerInterceptor2, once())
                .preHandle(eq(mockSet.request), eq(mockSet.requestMessage), eq(mockSet.handlerMethod),
                           eq(mockSet.args));

        verify(mockSet.handlerInterceptor1, once())
                .postHandle(eq(mockSet.request), eq(mockSet.requestMessage), eq(mockSet.responseMessage),
                            eq(mockSet.handlerMethod), eq(mockSet.args));
        verify(mockSet.handlerInterceptor2, once())
                .postHandle(eq(mockSet.request), eq(mockSet.requestMessage), eq(mockSet.responseMessage),
                            eq(mockSet.handlerMethod), eq(mockSet.args));
    }

    @Test
    @SneakyThrows
    public void invokeWithInterceptors_handlerInterceptor1_postHanle_throws_Exception() {
        mockSet.doReturn();

        RuntimeException cause = new RuntimeException();
        CEKHandlerInterceptException exception = new CEKHandlerInterceptException(mockSet.handlerMethod,
                                                                                  mockSet.args, cause);

        doThrow(cause).when(mockSet.handlerInterceptor1).postHandle(eq(mockSet.request),
                                                                    eq(mockSet.requestMessage),
                                                                    eq(mockSet.responseMessage),
                                                                    eq(mockSet.handlerMethod),
                                                                    eq(mockSet.args));

        assertThatThrownBy(() -> mockSet.dispatcher.invokeWithInterceptors(
                mockSet.request, mockSet.requestMessage, mockSet.sessionHolder, mockSet.handlerMethod,
                mockSet.args))
                .isExactlyInstanceOf(CEKHandlerInterceptException.class)
                .hasCause(cause)
                .isEqualToComparingOnlyGivenFields(exception, "handlerMethod", "args");

        verify(mockSet.handlerInterceptor1, once())
                .shouldNotIntercept(eq(mockSet.request), eq(mockSet.requestMessage), eq(mockSet.handlerMethod),
                                    eq(mockSet.args));
        verify(mockSet.handlerInterceptor2, once())
                .shouldNotIntercept(eq(mockSet.request), eq(mockSet.requestMessage), eq(mockSet.handlerMethod),
                                    eq(mockSet.args));

        verify(mockSet.handlerInterceptor1, once())
                .preHandle(eq(mockSet.request), eq(mockSet.requestMessage), eq(mockSet.handlerMethod),
                           eq(mockSet.args));
        verify(mockSet.handlerInterceptor2, once())
                .preHandle(eq(mockSet.request), eq(mockSet.requestMessage), eq(mockSet.handlerMethod),
                           eq(mockSet.args));

        verify(mockSet.handlerInterceptor1, once())
                .postHandle(eq(mockSet.request), eq(mockSet.requestMessage), eq(mockSet.responseMessage),
                            eq(mockSet.handlerMethod), eq(mockSet.args));
        verify(mockSet.handlerInterceptor2, once())
                .postHandle(eq(mockSet.request), eq(mockSet.requestMessage), eq(mockSet.responseMessage),
                            eq(mockSet.handlerMethod), eq(mockSet.args));
    }

    // handlerInterceptor2

    @Test
    @SneakyThrows
    public void invokeWithInterceptors_handlerInterceptor2_shouldNotIntercept_throws_CEKHandlerInterceptException() {
        mockSet.doReturn();

        CEKHandlerInterceptException exception = new CEKHandlerInterceptException(mockSet.handlerMethod,
                                                                                  mockSet.args,
                                                                                  new Exception());

        when(mockSet.handlerInterceptor2.shouldNotIntercept(eq(mockSet.request), eq(mockSet.requestMessage),
                                                            eq(mockSet.handlerMethod), eq(mockSet.args)))
                .thenThrow(exception);

        assertThatThrownBy(() -> mockSet.dispatcher.invokeWithInterceptors(
                mockSet.request, mockSet.requestMessage, mockSet.sessionHolder, mockSet.handlerMethod,
                mockSet.args))
                .isEqualTo(exception);

        verify(mockSet.handlerInterceptor1, once())
                .shouldNotIntercept(eq(mockSet.request), eq(mockSet.requestMessage), eq(mockSet.handlerMethod),
                                    eq(mockSet.args));
        verify(mockSet.handlerInterceptor2, once())
                .shouldNotIntercept(eq(mockSet.request), eq(mockSet.requestMessage), eq(mockSet.handlerMethod),
                                    eq(mockSet.args));

        verify(mockSet.handlerInterceptor1, never())
                .preHandle(eq(mockSet.request), eq(mockSet.requestMessage), eq(mockSet.handlerMethod),
                           eq(mockSet.args));
        verify(mockSet.handlerInterceptor2, never())
                .preHandle(eq(mockSet.request), eq(mockSet.requestMessage), eq(mockSet.handlerMethod),
                           eq(mockSet.args));

        verify(mockSet.handlerInterceptor1, never())
                .postHandle(eq(mockSet.request), eq(mockSet.requestMessage), isNull(),
                            eq(mockSet.handlerMethod), eq(mockSet.args));
        verify(mockSet.handlerInterceptor2, never())
                .postHandle(eq(mockSet.request), eq(mockSet.requestMessage), isNull(),
                            eq(mockSet.handlerMethod), eq(mockSet.args));
    }

    @Test
    @SneakyThrows
    public void invokeWithInterceptors_handlerInterceptor2_shouldNotIntercept_throws_Exception() {
        mockSet.doReturn();

        RuntimeException cause = new RuntimeException();

        when(mockSet.handlerInterceptor2.shouldNotIntercept(eq(mockSet.request), eq(mockSet.requestMessage),
                                                            eq(mockSet.handlerMethod), eq(mockSet.args)))
                .thenThrow(cause);

        assertThatThrownBy(() -> mockSet.dispatcher.invokeWithInterceptors(
                mockSet.request, mockSet.requestMessage, mockSet.sessionHolder, mockSet.handlerMethod,
                mockSet.args))
                .isEqualTo(cause);

        verify(mockSet.handlerInterceptor1, once())
                .shouldNotIntercept(eq(mockSet.request), eq(mockSet.requestMessage), eq(mockSet.handlerMethod),
                                    eq(mockSet.args));
        verify(mockSet.handlerInterceptor2, once())
                .shouldNotIntercept(eq(mockSet.request), eq(mockSet.requestMessage), eq(mockSet.handlerMethod),
                                    eq(mockSet.args));

        verify(mockSet.handlerInterceptor1, never())
                .preHandle(eq(mockSet.request), eq(mockSet.requestMessage), eq(mockSet.handlerMethod),
                           eq(mockSet.args));
        verify(mockSet.handlerInterceptor2, never())
                .preHandle(eq(mockSet.request), eq(mockSet.requestMessage), eq(mockSet.handlerMethod),
                           eq(mockSet.args));

        verify(mockSet.handlerInterceptor1, never())
                .postHandle(eq(mockSet.request), eq(mockSet.requestMessage), isNull(),
                            eq(mockSet.handlerMethod), eq(mockSet.args));
        verify(mockSet.handlerInterceptor2, never())
                .postHandle(eq(mockSet.request), eq(mockSet.requestMessage), isNull(),
                            eq(mockSet.handlerMethod), eq(mockSet.args));
    }

    @Test
    @SneakyThrows
    public void invokeWithInterceptors_handlerInterceptor2_preHandle_throws_CEKHandlerInterceptException() {
        mockSet.doReturn();

        CEKHandlerInterceptException exception = new CEKHandlerInterceptException(mockSet.handlerMethod,
                                                                                  mockSet.args,
                                                                                  new Exception());

        doThrow(exception).when(mockSet.handlerInterceptor2).preHandle(eq(mockSet.request),
                                                                       eq(mockSet.requestMessage),
                                                                       eq(mockSet.handlerMethod),
                                                                       eq(mockSet.args));

        assertThatThrownBy(() -> mockSet.dispatcher.invokeWithInterceptors(
                mockSet.request, mockSet.requestMessage, mockSet.sessionHolder, mockSet.handlerMethod,
                mockSet.args))
                .isEqualTo(exception);

        verify(mockSet.handlerInterceptor1, once())
                .shouldNotIntercept(eq(mockSet.request), eq(mockSet.requestMessage), eq(mockSet.handlerMethod),
                                    eq(mockSet.args));
        verify(mockSet.handlerInterceptor2, once())
                .shouldNotIntercept(eq(mockSet.request), eq(mockSet.requestMessage), eq(mockSet.handlerMethod),
                                    eq(mockSet.args));

        verify(mockSet.handlerInterceptor1, once())
                .preHandle(eq(mockSet.request), eq(mockSet.requestMessage), eq(mockSet.handlerMethod),
                           eq(mockSet.args));
        verify(mockSet.handlerInterceptor2, once())
                .preHandle(eq(mockSet.request), eq(mockSet.requestMessage), eq(mockSet.handlerMethod),
                           eq(mockSet.args));

        verify(mockSet.handlerInterceptor1, once())
                .postHandle(eq(mockSet.request), eq(mockSet.requestMessage), isNull(),
                            eq(mockSet.handlerMethod), eq(mockSet.args));
        verify(mockSet.handlerInterceptor2, once())
                .postHandle(eq(mockSet.request), eq(mockSet.requestMessage), isNull(),
                            eq(mockSet.handlerMethod), eq(mockSet.args));
    }

    @Test
    @SneakyThrows
    public void invokeWithInterceptors_handlerInterceptor2_preHanle_throws_Exception() {
        mockSet.doReturn();

        RuntimeException cause = new RuntimeException();
        CEKHandlerInterceptException exception = new CEKHandlerInterceptException(mockSet.handlerMethod,
                                                                                  mockSet.args, cause);

        doThrow(cause).when(mockSet.handlerInterceptor2).preHandle(eq(mockSet.request),
                                                                   eq(mockSet.requestMessage),
                                                                   eq(mockSet.handlerMethod), eq(mockSet.args));

        assertThatThrownBy(() -> mockSet.dispatcher.invokeWithInterceptors(
                mockSet.request, mockSet.requestMessage, mockSet.sessionHolder, mockSet.handlerMethod,
                mockSet.args))
                .isExactlyInstanceOf(CEKHandlerInterceptException.class)
                .hasCause(cause)
                .isEqualToComparingOnlyGivenFields(exception, "handlerMethod", "args");

        verify(mockSet.handlerInterceptor1, once())
                .shouldNotIntercept(eq(mockSet.request), eq(mockSet.requestMessage), eq(mockSet.handlerMethod),
                                    eq(mockSet.args));
        verify(mockSet.handlerInterceptor2, once())
                .shouldNotIntercept(eq(mockSet.request), eq(mockSet.requestMessage), eq(mockSet.handlerMethod),
                                    eq(mockSet.args));

        verify(mockSet.handlerInterceptor1, once())
                .preHandle(eq(mockSet.request), eq(mockSet.requestMessage), eq(mockSet.handlerMethod),
                           eq(mockSet.args));
        verify(mockSet.handlerInterceptor2, once())
                .preHandle(eq(mockSet.request), eq(mockSet.requestMessage), eq(mockSet.handlerMethod),
                           eq(mockSet.args));

        verify(mockSet.handlerInterceptor1, once())
                .postHandle(eq(mockSet.request), eq(mockSet.requestMessage), isNull(),
                            eq(mockSet.handlerMethod), eq(mockSet.args));
        verify(mockSet.handlerInterceptor2, once())
                .postHandle(eq(mockSet.request), eq(mockSet.requestMessage), isNull(),
                            eq(mockSet.handlerMethod), eq(mockSet.args));
    }

    @Test
    @SneakyThrows
    public void invokeWithInterceptors_handlerInterceptor2_postHandle_throws_CEKHandlerInterceptException() {
        mockSet.doReturn();

        CEKHandlerInterceptException exception = new CEKHandlerInterceptException(mockSet.handlerMethod,
                                                                                  mockSet.args,
                                                                                  new Exception());

        doThrow(exception).when(mockSet.handlerInterceptor2).postHandle(eq(mockSet.request),
                                                                        eq(mockSet.requestMessage),
                                                                        eq(mockSet.responseMessage),
                                                                        eq(mockSet.handlerMethod),
                                                                        eq(mockSet.args));

        assertThatThrownBy(() -> mockSet.dispatcher.invokeWithInterceptors(
                mockSet.request, mockSet.requestMessage, mockSet.sessionHolder, mockSet.handlerMethod,
                mockSet.args))
                .isEqualTo(exception);

        verify(mockSet.handlerInterceptor1, once())
                .shouldNotIntercept(eq(mockSet.request), eq(mockSet.requestMessage), eq(mockSet.handlerMethod),
                                    eq(mockSet.args));
        verify(mockSet.handlerInterceptor2, once())
                .shouldNotIntercept(eq(mockSet.request), eq(mockSet.requestMessage), eq(mockSet.handlerMethod),
                                    eq(mockSet.args));

        verify(mockSet.handlerInterceptor1, once())
                .preHandle(eq(mockSet.request), eq(mockSet.requestMessage), eq(mockSet.handlerMethod),
                           eq(mockSet.args));
        verify(mockSet.handlerInterceptor2, once())
                .preHandle(eq(mockSet.request), eq(mockSet.requestMessage), eq(mockSet.handlerMethod),
                           eq(mockSet.args));

        verify(mockSet.handlerInterceptor1, once())
                .postHandle(eq(mockSet.request), eq(mockSet.requestMessage), eq(mockSet.responseMessage),
                            eq(mockSet.handlerMethod), eq(mockSet.args));
        verify(mockSet.handlerInterceptor2, once())
                .postHandle(eq(mockSet.request), eq(mockSet.requestMessage), eq(mockSet.responseMessage),
                            eq(mockSet.handlerMethod), eq(mockSet.args));
    }

    @Test
    @SneakyThrows
    public void invokeWithInterceptors_handlerInterceptor2_postHanle_throws_Exception() {
        mockSet.doReturn();

        RuntimeException cause = new RuntimeException();
        CEKHandlerInterceptException exception = new CEKHandlerInterceptException(mockSet.handlerMethod,
                                                                                  mockSet.args, cause);

        doThrow(cause).when(mockSet.handlerInterceptor2).postHandle(eq(mockSet.request),
                                                                    eq(mockSet.requestMessage),
                                                                    eq(mockSet.responseMessage),
                                                                    eq(mockSet.handlerMethod),
                                                                    eq(mockSet.args));

        assertThatThrownBy(() -> mockSet.dispatcher.invokeWithInterceptors(
                mockSet.request, mockSet.requestMessage, mockSet.sessionHolder, mockSet.handlerMethod,
                mockSet.args))
                .isExactlyInstanceOf(CEKHandlerInterceptException.class)
                .hasCause(cause)
                .isEqualToComparingOnlyGivenFields(exception, "handlerMethod", "args");

        verify(mockSet.handlerInterceptor1, once())
                .shouldNotIntercept(eq(mockSet.request), eq(mockSet.requestMessage), eq(mockSet.handlerMethod),
                                    eq(mockSet.args));
        verify(mockSet.handlerInterceptor2, once())
                .shouldNotIntercept(eq(mockSet.request), eq(mockSet.requestMessage), eq(mockSet.handlerMethod),
                                    eq(mockSet.args));

        verify(mockSet.handlerInterceptor1, once())
                .preHandle(eq(mockSet.request), eq(mockSet.requestMessage), eq(mockSet.handlerMethod),
                           eq(mockSet.args));
        verify(mockSet.handlerInterceptor2, once())
                .preHandle(eq(mockSet.request), eq(mockSet.requestMessage), eq(mockSet.handlerMethod),
                           eq(mockSet.args));

        verify(mockSet.handlerInterceptor1, once())
                .postHandle(eq(mockSet.request), eq(mockSet.requestMessage), eq(mockSet.responseMessage),
                            eq(mockSet.handlerMethod), eq(mockSet.args));
        verify(mockSet.handlerInterceptor2, once())
                .postHandle(eq(mockSet.request), eq(mockSet.requestMessage), eq(mockSet.responseMessage),
                            eq(mockSet.handlerMethod), eq(mockSet.args));
    }

    @Test
    @SneakyThrows
    public void invokeWithInterceptors_dispatcher_throws_Exception() {
        RuntimeException cause = new RuntimeException();
        mockSet.doThrow(cause);

        assertThatThrownBy(() -> mockSet.dispatcher.invokeWithInterceptors(
                mockSet.request, mockSet.requestMessage, mockSet.sessionHolder, mockSet.handlerMethod,
                mockSet.args))
                .isEqualTo(cause);

        verify(mockSet.handlerInterceptor1, once())
                .shouldNotIntercept(eq(mockSet.request), eq(mockSet.requestMessage), eq(mockSet.handlerMethod),
                                    eq(mockSet.args));
        verify(mockSet.handlerInterceptor2, once())
                .shouldNotIntercept(eq(mockSet.request), eq(mockSet.requestMessage), eq(mockSet.handlerMethod),
                                    eq(mockSet.args));

        verify(mockSet.handlerInterceptor1, once())
                .preHandle(eq(mockSet.request), eq(mockSet.requestMessage), eq(mockSet.handlerMethod),
                           eq(mockSet.args));
        verify(mockSet.handlerInterceptor2, once())
                .preHandle(eq(mockSet.request), eq(mockSet.requestMessage), eq(mockSet.handlerMethod),
                           eq(mockSet.args));

        verify(mockSet.handlerInterceptor1, once())
                .postHandle(eq(mockSet.request), eq(mockSet.requestMessage), isNull(),
                            eq(mockSet.handlerMethod), eq(mockSet.args));
        verify(mockSet.handlerInterceptor2, once())
                .postHandle(eq(mockSet.request), eq(mockSet.requestMessage), isNull(),
                            eq(mockSet.handlerMethod), eq(mockSet.args));
    }

    private static VerificationMode once() {
        return times(1);
    }

    private static VerificationMode twice() {
        return times(2);
    }

    @Data
    class MockSet {

        CEKHandlerInterceptor handlerInterceptor1 = mock(CEKHandlerInterceptor.class);
        CEKHandlerInterceptor handlerInterceptor2 = mock(CEKHandlerInterceptor.class);

        CEKRequestHandlerDispatcher dispatcher = spy(new CEKRequestHandlerDispatcher(null, null, null));

        HttpServletRequest request = mock(HttpServletRequest.class);
        CEKRequestMessage requestMessage = mock(CEKRequestMessage.class);
        Method dummyMethod = Optional.ofNullable(ReflectionUtils.findMethod(
                CEKRequestHandlerDispatcherInterceptorTest.class, "dummy",
                Object.class, Object.class, Object.class)).orElseThrow(
                () -> new RuntimeException("Not found dummy method"));
        CEKHandlerMethod handlerMethod =
                spy(CEKHandlerMethod.builder()
                                    .requestType(RequestType.EVENT)
                                    .bean(CEKRequestHandlerDispatcherInterceptorTest.this)
                                    .method(dummyMethod)
                                    .name("Clova.Event")
                                    .methodParams(Arrays.asList(
                                            new MethodParameter(dummyMethod, 0),
                                            new MethodParameter(dummyMethod, 1),
                                            new MethodParameter(dummyMethod, 2)
                                    ))
                                    .build());

        SessionHolder sessionHolder = mock(SessionHolder.class);

        Object[] args = new Object[] { mock(Object.class), mock(Object.class), mock(Object.class) };

        CEKResponse response = mock(CEKResponse.class);

        CEKResponseMessage responseMessage = new CEKResponseMessage(response);

        List<CEKHandlerInterceptor> handlerInterceptors;

        MockSet() {
            this.handlerInterceptors = Lists.newArrayList(this.handlerInterceptor1, this.handlerInterceptor2);
            // Check NPE.
            this.handlerMethod.toString();
        }

        public void doReturn() {
            Mockito.doReturn(response).when(handlerMethod).invoke(eq(args));
        }

        public void doThrow(Throwable t) {
            Mockito.doThrow(t).when(handlerMethod).invoke(eq(args));
        }
    }

    private void dummy(Object arg1, Object arg2, Object arg3) {
    }
}
