package com.snackecommerce.product.dto;

import com.snackecommerce.product.enums.DiscountType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponRequest {
    @NotBlank(message = "Coupon code is required")
    @Size(min = 3, max = 20, message = "Coupon code must be 3-20 characters")
    private String code;

    @NotNull(message = "Discount type is required")
    private DiscountType discountType;

    @NotNull(message = "Discount value is required")
    @Positive(message = "Discount value must be positive")
    private Double discountValue;

    @PositiveOrZero(message = "Minimum order amount cannot be negative")
    private Double minOrderAmount;

    @Positive(message = "Max usage per user must be positive")
    private Integer maxUsagePerUser;

    @Positive(message = "Total usage limit must be positive")
    private Integer totalUsageLimit;

    @NotNull(message = "Valid from date is required")
    private LocalDateTime validFrom;

    @NotNull(message = "Valid till date is required")
    private LocalDateTime validTill;
}
