package com.snackecommerce.common.exception;

public class FAQNotFoundException extends RuntimeException {
    
    public FAQNotFoundException(String message) {
        super(message);
    }
    
    public FAQNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
