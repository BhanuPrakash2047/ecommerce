package com.snackecommerce.order.enums;

public enum ShipmentJobStatus {
    PENDING,    // Waiting to be retried
    SUCCESS,    // Shipment created successfully
    FAILED      // Max attempts reached, manual intervention needed
}
