package com.snackecommerce.common.exception;

public class InvalidProductDataException extends RuntimeException {
    
    public InvalidProductDataException(String message) {
        super(message);
    }
    
    public InvalidProductDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
