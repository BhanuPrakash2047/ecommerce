package com.snackecommerce.delivery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response for tracking order delivery status
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrackingResponse {
    private Long orderId;
    private String waybillNumber;
    private String currentStatus;  // IN_TRANSIT, DELIVERED, FAILED, PENDING
    private String location;
    private String lastUpdate;
    private Boolean isDelivered;
    private String estimatedDeliveryDate;
}
