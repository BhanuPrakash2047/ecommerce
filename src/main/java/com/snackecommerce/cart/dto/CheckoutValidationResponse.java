package com.snackecommerce.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckoutValidationResponse {
    private Boolean isValid;
    private String message;
    private List<String> issues;  // Why validation failed
    private CheckoutDetails checkoutDetails;  // If valid, final amounts
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CheckoutDetails {
        private Integer totalItems;
        private BigDecimal subtotal;
        private BigDecimal discountAmount;
        private Long appliedCouponId;
        private String appliedCouponCode;
        private BigDecimal finalTotal;
        private List<CartItemResponse> items;
    }
}
