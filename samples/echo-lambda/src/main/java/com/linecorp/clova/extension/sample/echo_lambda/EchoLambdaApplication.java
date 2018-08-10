package com.linecorp.clova.extension.sample.echo_lambda;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@SpringBootApplication
public class EchoLambdaApplication extends SpringBootServletInitializer {
    /**
     * SpringBoot entry point.
     */
    public static void main(String[] args) {
        SpringApplication.run(EchoLambdaApplication.class, args);
    }

    @EnableWebMvc
    @Configuration
    static class MvcConfig {

        @Bean
        public HandlerMapping handlerMapping() {
            return new RequestMappingHandlerMapping();
        }

        @Bean
        public HandlerAdapter handlerAdapter() {
            return new RequestMappingHandlerAdapter();
        }

        @Bean
        public HandlerExceptionResolver handlerExceptionResolver() {
            return (request, response, handler, ex) -> null;
        }
    }

}
