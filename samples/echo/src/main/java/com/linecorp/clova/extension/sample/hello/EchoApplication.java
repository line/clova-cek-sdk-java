package com.linecorp.clova.extension.sample.hello;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EchoApplication {
    /**
     * SpringBoot entry point.
     */
    public static void main(String[] args) {
        SpringApplication.run(EchoApplication.class, args);
    }
}
