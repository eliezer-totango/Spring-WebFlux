package com.self;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication()
@ComponentScan(basePackages = {
        "com.self.config",
        "com.self.controllers",
        "com.self.services"
})
public class SelfApp extends SpringBootServletInitializer {
    public static void main(String[] args) {
        SpringApplication.run(SelfApp.class, args);
    }
}