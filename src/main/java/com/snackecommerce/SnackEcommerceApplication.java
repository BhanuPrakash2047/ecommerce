package com.snackecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableScheduling  // Enable @Scheduled tasks
@EnableAsync       // Enable @Async for async method execution
@EnableRetry       // Enable @Retryable for optimistic locking
public class SnackEcommerceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SnackEcommerceApplication.class, args);
    }

}
