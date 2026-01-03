package com.snackecommerce.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {
    private Long id;
    private String name;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Boolean isAvailable;
    private Boolean isEligibleForCoupon;
    private LocalDateTime createdAt;
}
