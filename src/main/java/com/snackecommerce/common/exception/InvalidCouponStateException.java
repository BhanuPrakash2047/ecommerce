package com.snackecommerce.common.exception;

public class InvalidCouponStateException extends RuntimeException {
    public InvalidCouponStateException(String message) {
        super(message);
    }

    public InvalidCouponStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
