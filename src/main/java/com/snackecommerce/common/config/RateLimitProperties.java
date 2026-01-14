package com.snackecommerce.common.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration properties for rate limiting
 * Defines rate limit rules for different endpoints
 */
@Data
@Component
@ConfigurationProperties(prefix = "rate-limit")
public class RateLimitProperties {

    private RateLimitRule defaultRule = new RateLimitRule(100, 1); // 100 requests per minute
    private Map<String, RateLimitRule> rules = new HashMap<>();

    public RateLimitProperties() {
        // Initialize with default rules
        rules.put("login", new RateLimitRule(5, 15)); // 5 attempts per 15 minutes
        rules.put("register", new RateLimitRule(3, 60)); // 3 attempts per hour
        rules.put("password-reset", new RateLimitRule(5, 60)); // 5 attempts per hour
        rules.put("payment", new RateLimitRule(10, 60)); // 10 attempts per hour
        rules.put("products", new RateLimitRule(200, 1)); // 100 requests per minute
        rules.put("cart", new RateLimitRule(50, 1)); // 50 requests per minute
        rules.put("orders", new RateLimitRule(20, 60)); // 20 requests per hour
        rules.put("upload", new RateLimitRule(10, 60)); // 10 uploads per hour
    }

    /**
     * Rate limit rule with capacity and duration
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RateLimitRule {
        private long capacity; // Number of requests allowed
        private long durationMinutes; // Time window in minutes
    }
}
