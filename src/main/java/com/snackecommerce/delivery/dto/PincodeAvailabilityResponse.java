package com.snackecommerce.delivery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response for pincode availability check
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PincodeAvailabilityResponse {
    private String pincode;
    private Boolean isAvailable;
    private String status;  // SERVICEABLE, NON_SERVICEABLE, PARTIALLY_SERVICEABLE
    private Double estimatedDeliveryDays;
    private String region;
    private String state;
}
