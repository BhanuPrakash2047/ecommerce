package com.snackecommerce.common.annotation;
import java.lang.annotation.*;

/**
 * Annotation to mark endpoints that should be rate limited
 * Usage: @RateLimit("login") on controller methods
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /**
     * The endpoint name for rate limiting rule lookup
     * Examples: "login", "register", "payment", "products"
     */
    String value() default "default";

    /**
     * Whether to use authenticated user for rate limiting (true)
     * or IP address (false)
     */
    boolean useAuthenticatedUser() default true;
}
