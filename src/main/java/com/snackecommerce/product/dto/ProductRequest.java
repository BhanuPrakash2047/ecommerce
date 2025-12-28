package com.snackecommerce.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequest {
    private String name;
    private Double price;
    private Integer stockQuantity;
    private Boolean active;
    private Boolean isEligibleForCoupon;
}
