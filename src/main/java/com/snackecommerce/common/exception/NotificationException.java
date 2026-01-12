package com.snackecommerce.common.exception;

/**
 * Exception thrown when notification sending fails
 * 
 * This is a non-critical exception that should NOT rollback the main transaction.
 * Used with @Transactional(noRollbackFor = NotificationException.class)
 * 
 * Example usage:
 * - Payment succeeded but notification failed
 * - Order created but email couldn't be sent
 * - Shipment update notification failed
 */
public class NotificationException extends RuntimeException {
    public NotificationException(String message) {
        super(message);
    }

    public NotificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
