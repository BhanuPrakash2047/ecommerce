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
public class CartResponse {
    private Long cartId;
    private Long userId;
    private List<CartItemResponse> items;
    private BigDecimal subtotal;           // Sum of all items (no discount)
    private BigDecimal discountAmount;     // Applied coupon discount
    private BigDecimal total;              // subtotal - discount
    private Long appliedCouponId;
    private String appliedCouponCode;
    private List<String> alerts;           // Price changes, deleted products, etc.
}
