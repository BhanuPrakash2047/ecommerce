package com.snackecommerce.payment.config;

import com.razorpay.RazorpayClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration for Razorpay Payment Gateway
 * Initializes RazorpayClient bean with API credentials
 * 
 * Production Note: Use environment variables for sensitive credentials
 */
@Configuration
public class RazorpayConfig {

    private static final Logger logger = LoggerFactory.getLogger(RazorpayConfig.class);

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    /**
     * Create and configure RazorpayClient bean
     * This bean is used to interact with Razorpay API
     */
    @Bean
    public RazorpayClient razorpayClient() throws Exception {
        if (razorpayKeyId == null || razorpayKeyId.equals("YOUR_RAZOPAY_KEY_ID")) {
            throw new RuntimeException("Razorpay credentials not configured. Please set razorpay.key.id and razorpay.key.secret in application.properties");
        }
        
        logger.info("Initializing Razorpay client with key ID: {}", razorpayKeyId.substring(0, Math.min(10, razorpayKeyId.length())) + "...");
        return new RazorpayClient(razorpayKeyId, razorpayKeySecret);
    }
}
