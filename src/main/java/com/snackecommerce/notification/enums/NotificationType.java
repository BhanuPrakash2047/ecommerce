package com.snackecommerce.notification.enums;

public enum NotificationType {
    // User notifications
    PAYMENT_RECEIVED,           // Payment received, order processing
    SHIPMENT_CREATED,           // Shipment created with tracking
    ORDER_DELIVERED,            // Order delivered
    PASSWORD_CHANGED,           // Password changed successfully
    
    // Admin notifications
    ADMIN_SHIPMENT_FAILED       // Shipment job failed after max retries
}
