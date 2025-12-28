package com.snackecommerce.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponValidationResponse {
    private Boolean isValid;
    private String message;
    private String errorCode;  // EXPIRED, LIMIT_EXCEEDED, MIN_ORDER_NOT_MET, INACTIVE, NOT_FOUND
    private CouponResponse coupon;
    private Double estimatedDiscount;
}
