package com.snackecommerce.common.exception;

public class CheckoutValidationException extends RuntimeException {
    public CheckoutValidationException(String message) {
        super(message);
    }
}
