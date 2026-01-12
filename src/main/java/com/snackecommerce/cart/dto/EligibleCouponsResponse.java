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
        private Long id;  // Changed from couponId to id
        private String code;
        private String description;
        private java.math.BigDecimal discountValue;  // Changed from discountAmount to discountValue
        private String type;  // Changed from discountType to type (PERCENTAGE, FLAT)
        private java.math.BigDecimal minOrderAmount;  // Added: Minimum order amount required
        private Boolean active;  // Added: Is coupon active
        private java.time.LocalDateTime validFrom;  // Added: Validity start date
        private java.time.LocalDateTime validTill;  // Added: Validity end date
        private Boolean isEligible;
        private String reason;  // If not eligible: why not
    }
    
    private List<CouponOption> eligibleCoupons;
    private List<CouponOption> ineligibleCoupons;
}
