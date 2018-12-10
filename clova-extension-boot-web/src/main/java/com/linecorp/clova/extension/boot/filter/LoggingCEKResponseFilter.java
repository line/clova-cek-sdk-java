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

package com.linecorp.clova.extension.boot.filter;

import java.io.IOException;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.Ordered;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import com.linecorp.clova.extension.boot.filter.logger.HttpServletResponseLogger;

import lombok.Getter;
import lombok.Setter;

/**
 * {@link javax.servlet.Filter Filter} for logging CEK response.
 * <p>
 * Writes the response out using {@link HttpServletResponseLogger}s of Spring Beans. Where to write depends on
 * the {@link HttpServletResponseLogger} implementation.
 * <p>
 * This class calls {@link ContentCachingResponseWrapper#copyBodyToResponse()}.
 *
 * @see HttpServletResponseLogger
 */
public class LoggingCEKResponseFilter extends OncePerRequestFilter implements Ordered {

    private final List<HttpServletResponseLogger> loggers;
    private final boolean shouldNotFilter;

    private final String cekApiPathPattern;

    @Getter
    @Setter
    private int order;

    private AntPathMatcher pathMatcher = new AntPathMatcher();

    public LoggingCEKResponseFilter(List<HttpServletResponseLogger> loggers, String cekApiPathPattern) {
        this.loggers = loggers;
        this.shouldNotFilter = CollectionUtils.isEmpty(loggers);
        this.cekApiPathPattern = cekApiPathPattern;
    }

    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return true;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        if (shouldNotFilter) {
            return true;
        }
        return !pathMatcher.match(cekApiPathPattern, request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws
                                                             ServletException, IOException {
        ContentCachingResponseWrapper responseToUse = wrapContentCaching(response);

        try {
            filterChain.doFilter(request, responseToUse);
        } finally {
            logResponse(responseToUse);
            responseToUse.copyBodyToResponse();
        }
    }

    private void logResponse(ContentCachingResponseWrapper response) {
        loggers.forEach(logger -> logger.log(response));
    }

    private static ContentCachingResponseWrapper wrapContentCaching(HttpServletResponse response) {
        if (response instanceof ContentCachingResponseWrapper) {
            return (ContentCachingResponseWrapper) response;
        }
        return new ContentCachingResponseWrapper(response);
    }

}
