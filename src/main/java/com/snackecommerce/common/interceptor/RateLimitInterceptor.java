package com.snackecommerce.common.interceptor;

import com.snackecommerce.common.annotation.RateLimit;
import com.snackecommerce.common.config.RateLimitConfig;
import com.snackecommerce.common.config.RateLimitProperties;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

/**
 * HTTP Interceptor for rate limiting
 * Intercepts requests and checks if they exceed rate limit
 * Returns 429 Too Many Requests if limit exceeded
 */
@Slf4j
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    @Autowired
    private RateLimitConfig rateLimitConfig;

    @Autowired
    private RateLimitProperties rateLimitProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Check if handler has @RateLimit annotation
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RateLimit rateLimitAnnotation = handlerMethod.getMethodAnnotation(RateLimit.class);

        // If no @RateLimit annotation, allow request
        if (rateLimitAnnotation == null) {
            return true;
        }

        // Get the endpoint name and rule
        String endpoint = rateLimitAnnotation.value();
        RateLimitProperties.RateLimitRule rule = rateLimitConfig.getRuleByEndpoint(endpoint);

        // Determine the key for rate limiting (user or IP)
        String limitKey = getLimitKey(rateLimitAnnotation, request);

        // Check rate limit against Redis
        boolean allowed = rateLimitConfig.checkRateLimit(limitKey, rule);

        if (allowed) {
            // Request allowed, add rate limit headers
            long remainingRequests = rateLimitConfig.getRemainingRequests(limitKey, rule);
            response.addHeader("X-Rate-Limit-Limit", String.valueOf(rule.getCapacity()));
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(remainingRequests));
            response.addHeader("X-Rate-Limit-Reset", String.valueOf(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(rule.getDurationMinutes())));

            log.debug("Rate limit check passed for endpoint: {}, key: {}, remaining: {}", 
                endpoint, limitKey, remainingRequests);
            return true;
        } else {
            // Request denied, return 429
            long waitForRefill = rule.getDurationMinutes() * 60;
            response.addHeader("X-Rate-Limit-Limit", String.valueOf(rule.getCapacity()));
            response.addHeader("X-Rate-Limit-Remaining", "0");
            response.addHeader("X-Rate-Limit-Reset", String.valueOf(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(waitForRefill)));
            response.addHeader("Retry-After", String.valueOf(waitForRefill));

            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write(buildErrorResponse(endpoint, waitForRefill));

            log.warn("Rate limit exceeded for endpoint: {}, key: {}, wait seconds: {}", 
                endpoint, limitKey, waitForRefill);
            return false;
        }
    }

    /**
     * Determine the rate limit key based on annotation configuration
     * If useAuthenticatedUser=true, uses authenticated user's email/username
     * Otherwise, uses client IP address
     */
    private String getLimitKey(RateLimit annotation, HttpServletRequest request) {
        if (annotation.useAuthenticatedUser()) {
            // Try to get authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && 
                !authentication.getName().equals("anonymousUser")) {
                log.debug("=================>Using authenticated user for rate limiting: {}", authentication.getName());
                return "user:" + annotation.value() + ":" + authentication.getName();
            }
        }

        // Fall back to IP address
        String clientIp = getClientIpAddress(request);
        return "ip:" + annotation.value() + ":" + clientIp;
    }

    /**
     * Extract client IP address from request
     * Handles X-Forwarded-For header for proxied requests
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    /**
     * Build JSON error response for rate limit exceeded
     */
    private String buildErrorResponse(String endpoint, long waitSeconds) {
        return String.format(
            "{\"error\": \"Too Many Requests\", \"message\": \"Rate limit exceeded for endpoint: %s. Please wait %d seconds before retrying.\", \"retryAfter\": %d}",
            endpoint, waitSeconds, waitSeconds
        );
    }
}
