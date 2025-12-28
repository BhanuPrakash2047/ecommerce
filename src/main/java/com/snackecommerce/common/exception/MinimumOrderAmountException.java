package com.snackecommerce.common.exception;

public class MinimumOrderAmountException extends RuntimeException {
    public MinimumOrderAmountException(String message) {
        super(message);
    }

    public MinimumOrderAmountException(String message, Throwable cause) {
        super(message, cause);
    }
}
