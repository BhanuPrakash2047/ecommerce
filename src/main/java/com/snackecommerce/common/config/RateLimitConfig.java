package com.snackecommerce.common.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Configuration for rate limiting using Redis counters
 * Stores rate limit counters in Upstash Redis for distributed rate limiting
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(RateLimitProperties.class)
public class RateLimitConfig {

    @Autowired
    private RateLimitProperties rateLimitProperties;

    @Value("${spring.redis.host}")
    private String redisHost;

    @Value("${spring.redis.port}")
    private int redisPort;

    @Value("${spring.redis.password}")
    private String redisPassword;

    private JedisPool jedisPool;
    private final ConcurrentHashMap<String, Bucket> inMemoryBuckets = new ConcurrentHashMap<>();

    /**
     * Initialize Jedis Pool for Upstash Redis connection
     * Called lazily when first rate limit check is requested
     */
    private synchronized JedisPool getJedisPool() {
        if (jedisPool == null) {
            try {
                JedisPoolConfig poolConfig = new JedisPoolConfig();
                poolConfig.setMaxTotal(8);
                poolConfig.setMaxIdle(8);
                poolConfig.setMinIdle(0);
                poolConfig.setTestOnBorrow(true);
                poolConfig.setTestOnReturn(true);
                poolConfig.setTestWhileIdle(true);
                poolConfig.setMinEvictableIdleTimeMillis(Duration.ofSeconds(60).toMillis());
                poolConfig.setTimeBetweenEvictionRunsMillis(Duration.ofSeconds(30).toMillis());
                poolConfig.setNumTestsPerEvictionRun(3);
                poolConfig.setBlockWhenExhausted(true);

                this.jedisPool = new JedisPool(
                    poolConfig,
                    redisHost,
                    redisPort,
                    60000,
                    redisPassword,
                    0,
                    true
                );
                log.info("✅ Jedis Pool initialized - Connected to Upstash Redis");
            } catch (Exception e) {
                log.warn("⚠️ Failed to initialize Jedis Pool: {}", e.getMessage());
                this.jedisPool = null;
            }
        }
        return this.jedisPool;
    }

    /**
     * Check and enforce rate limit using Redis counters
     * Returns true if request is allowed, false if limit exceeded
     */
    public boolean checkRateLimit(String key, RateLimitProperties.RateLimitRule rule) {
        try {
            JedisPool pool = getJedisPool();
            if (pool != null) {
                Jedis jedis = pool.getResource();
                try {
                    String redisKey = "rate:" + key;
                    long capacity = rule.getCapacity();
                    long durationSeconds = rule.getDurationMinutes() * 60;
                    
                    // Get current count from Redis
                    String countStr = jedis.get(redisKey);
                    long count = countStr != null ? Long.parseLong(countStr) : 0;
                    
                    log.debug("🔍 Redis check: key={}, count={}, capacity={}", redisKey, count, capacity);
                    
                    if (count >= capacity) {
                        // Rate limit exceeded
                        log.warn("❌ Rate limit EXCEEDED: {} ({}/{})", redisKey, count, capacity);
                        return false;
                    }
                    
                    // Increment counter in Redis
                    long newCount = jedis.incr(redisKey);
                    
                    // Set expiration only on first increment
                    if (newCount == 1) {
                        jedis.expire(redisKey, (int) durationSeconds);
                        log.info("🔑 NEW rate limit key created in Upstash Redis: {} (TTL: {}s)", redisKey, durationSeconds);
                    }
                    
                    log.debug("✅ Rate limit PASSED: {} ({}/{})", redisKey, newCount, capacity);
                    return true;
                    
                } catch (Exception e) {
                    log.warn("⚠️ Redis operation failed: {}, falling back to in-memory", e.getMessage());
                    return checkInMemoryRateLimit(key, rule);
                } finally {
                    jedis.close();
                }
            }
        } catch (Exception e) {
            log.warn("⚠️ Failed to get Redis connection: {}", e.getMessage());
        }
        
        // Fallback to in-memory if Redis unavailable
        return checkInMemoryRateLimit(key, rule);
    }

    /**
     * In-memory fallback rate limiting using Bucket4j
     */
    private boolean checkInMemoryRateLimit(String key, RateLimitProperties.RateLimitRule rule) {
        Bucket bucket = inMemoryBuckets.computeIfAbsent(key, k -> createBucket(rule));
        boolean allowed = bucket.tryConsume(1);
        
        if (allowed) {
            log.debug("📦 In-memory rate limit PASSED: {}", key);
        } else {
            log.warn("❌ In-memory rate limit EXCEEDED: {}", key);
        }
        
        return allowed;
    }

    /**
     * Create a new in-memory bucket with the specified rule
     */
    private Bucket createBucket(RateLimitProperties.RateLimitRule rule) {
        return Bucket4j.builder()
            .addLimit(Bandwidth.classic(
                rule.getCapacity(),
                Refill.intervally(rule.getCapacity(), Duration.ofMinutes(rule.getDurationMinutes()))
            ))
            .build();
    }

    /**
     * Get rate limit rule by endpoint name
     */
    public RateLimitProperties.RateLimitRule getRuleByEndpoint(String endpoint) {
        return rateLimitProperties.getRules().getOrDefault(
            endpoint,
            rateLimitProperties.getDefaultRule()
        );
    }

    /**
     * Get remaining requests for a key (for response headers)
     */
    public long getRemainingRequests(String key, RateLimitProperties.RateLimitRule rule) {
        try {
            JedisPool pool = getJedisPool();
            if (pool != null) {
                Jedis jedis = pool.getResource();
                try {
                    String redisKey = "rate:" + key;
                    String countStr = jedis.get(redisKey);
                    long count = countStr != null ? Long.parseLong(countStr) : 0;
                    return Math.max(0, rule.getCapacity() - count);
                } finally {
                    jedis.close();
                }
            }
        } catch (Exception e) {
            log.debug("Failed to get remaining requests from Redis: {}", e.getMessage());
        }
        return rule.getCapacity();
    }
}
