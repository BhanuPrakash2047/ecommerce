package com.snackecommerce.common.exception;

public class InvalidCartItemException extends RuntimeException {
    public InvalidCartItemException(String message) {
        super(message);
    }
}
