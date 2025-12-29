package com.snackecommerce.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EligibleCouponsResponse {
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CouponOption {
        private Long couponId;
        private String code;
        private String description;
        private java.math.BigDecimal discountAmount;
        private String discountType;  // PERCENTAGE, FLAT
        private Boolean isEligible;
        private String reason;  // If not eligible: why not
    }
    
    private List<CouponOption> eligibleCoupons;
    private List<CouponOption> ineligibleCoupons;
}
