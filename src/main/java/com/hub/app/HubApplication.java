package com.hub.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class HubApplication {
    public static void main(String[] args) {
        SpringApplication.run(HubApplication.class, args);
    }
}
