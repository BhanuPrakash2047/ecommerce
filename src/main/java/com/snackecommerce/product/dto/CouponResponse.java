package com.snackecommerce.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.snackecommerce.product.entity.Coupon.CouponType;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponResponse {
    private Long id;
    private String code;
    private CouponType type;
    private Double discountValue;
    private Double minOrderAmount;
    private Boolean active;
    private LocalDateTime validFrom;
    private LocalDateTime validTill;
}
