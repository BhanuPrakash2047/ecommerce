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
public class ProductResponse {
    private Long id;
    private String name;
    private Double price;
    private Integer stockQuantity;
    private Boolean active;
    private Boolean isEligibleForCoupon;
    private Double averageRating;
    private Long reviewCount;
    private LocalDateTime createdAt;
}
