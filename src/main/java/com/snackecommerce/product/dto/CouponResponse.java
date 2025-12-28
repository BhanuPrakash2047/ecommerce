package com.snackecommerce.product.dto;

import com.snackecommerce.product.enums.DiscountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponResponse {
    private Long id;
    private String code;
    private DiscountType discountType;
    private Double discountValue;
    private Double minOrderAmount;
    private Integer maxUsagePerUser;
    private Integer totalUsageLimit;
    private Integer usedCount;
    private Boolean active;
    private LocalDateTime validFrom;
    private LocalDateTime validTill;
}
