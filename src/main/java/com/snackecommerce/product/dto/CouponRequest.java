package com.snackecommerce.product.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.snackecommerce.product.entity.Coupon.CouponType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponRequest {
    @NotBlank(message = "Coupon code is required")
    @Size(min = 3, max = 20, message = "Coupon code must be 3-20 characters")
    private String code;

    @NotNull(message = "Coupon type is required")
    private CouponType type;  // FLAT or PERCENTAGE

    @NotNull(message = "Discount value is required")
    @Positive(message = "Discount value must be positive")
    private BigDecimal discountValue;  // Rupees for FLAT, percentage for PERCENTAGE

    @PositiveOrZero(message = "Minimum order amount must be zero or positive")
    private BigDecimal minOrderAmount;  // Minimum cart total required (e.g., 500 for min ₹500)

    @NotNull(message = "Valid from date is required")
    private LocalDateTime validFrom;

    @NotNull(message = "Valid till date is required")
    private LocalDateTime validTill;

    private Boolean active;
}
