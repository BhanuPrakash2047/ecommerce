package com.snackecommerce.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartCouponResponse {
    private Long cartId;
    private CouponResponse coupon;
    private Double discountAmount;
    private Double cartTotal;
    private Double cartTotalAfterDiscount;
}
