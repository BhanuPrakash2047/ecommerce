package com.snackecommerce.common.config;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.SslOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis Cache Configuration
 * Uses existing Redis (Upstash) credentials for caching with TTL safety guards
 * SSL enabled for Upstash connection
 */
@Configuration
@EnableCaching
public class RedisCacheConfig {

    @Value("${spring.redis.host}")
    private String redisHost;

    @Value("${spring.redis.port}")
    private int redisPort;

    @Value("${spring.redis.password}")
    private String redisPassword;

    // Cache TTL configuration (in minutes)
    private static final long PRODUCTS_CACHE_TTL = 30;      // 30 minutes - products change rarely
    private static final long CART_CACHE_TTL = 15;          // 15 minutes - user-specific, moderate TTL
    private static final long MEDIA_CACHE_TTL = 60;         // 60 minutes - images/videos metadata rarely change
    private static final long REVIEWS_CACHE_TTL = 30;       // 30 minutes - reviews update occasionally
    private static final long FAQS_CACHE_TTL = 60;          // 60 minutes - FAQs rarely change

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setHostName(redisHost);
        redisConfig.setPort(redisPort);
        redisConfig.setPassword(redisPassword);

        // Enable SSL for Upstash Redis connection
        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .useSsl()
                .disablePeerVerification()  // Upstash uses self-signed certs
                .commandTimeout(Duration.ofSeconds(60))
                .build();

        return new LettuceConnectionFactory(redisConfig, clientConfig);
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Default cache configuration with 30 minutes TTL
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        // Cache-specific TTL configurations
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // Products cache - single key for all products
        cacheConfigurations.put("products", defaultConfig.entryTtl(Duration.ofMinutes(PRODUCTS_CACHE_TTL)));
        cacheConfigurations.put("availableProducts", defaultConfig.entryTtl(Duration.ofMinutes(PRODUCTS_CACHE_TTL)));
        cacheConfigurations.put("couponEligibleProducts", defaultConfig.entryTtl(Duration.ofMinutes(PRODUCTS_CACHE_TTL)));
        cacheConfigurations.put("product", defaultConfig.entryTtl(Duration.ofMinutes(PRODUCTS_CACHE_TTL)));

        // Cart cache - user-specific
        cacheConfigurations.put("cart", defaultConfig.entryTtl(Duration.ofMinutes(CART_CACHE_TTL)));
        cacheConfigurations.put("eligibleCoupons", defaultConfig.entryTtl(Duration.ofMinutes(CART_CACHE_TTL)));

        // Media cache - product-specific
        cacheConfigurations.put("productImages", defaultConfig.entryTtl(Duration.ofMinutes(MEDIA_CACHE_TTL)));
        cacheConfigurations.put("productVideos", defaultConfig.entryTtl(Duration.ofMinutes(MEDIA_CACHE_TTL)));

        // Reviews cache - product-specific
        cacheConfigurations.put("productReviews", defaultConfig.entryTtl(Duration.ofMinutes(REVIEWS_CACHE_TTL)));

        // FAQs cache - product-specific
        cacheConfigurations.put("productFAQs", defaultConfig.entryTtl(Duration.ofMinutes(FAQS_CACHE_TTL)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();
    }
}
