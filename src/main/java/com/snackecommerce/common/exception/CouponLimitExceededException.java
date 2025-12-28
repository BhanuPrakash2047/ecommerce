package com.snackecommerce.common.exception;

public class CouponLimitExceededException extends RuntimeException {
    public CouponLimitExceededException(String message) {
        super(message);
    }

    public CouponLimitExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}
