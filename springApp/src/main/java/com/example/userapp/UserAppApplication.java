package com.example.userapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Main Spring Boot application class.
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.example.userapp")
public class UserAppApplication {

    public static void main(final String[] args) {
        SpringApplication.run(UserAppApplication.class, args);
    }
}
