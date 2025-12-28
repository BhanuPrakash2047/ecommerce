package com.snackecommerce.common.exception;

public class CouponExpiredException extends RuntimeException {
    public CouponExpiredException(String message) {
        super(message);
    }

    public CouponExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}
