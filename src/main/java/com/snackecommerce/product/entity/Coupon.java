package com.snackecommerce.product.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    @Enumerated(EnumType.STRING)
    private CouponType type;  // FLAT or PERCENTAGE

    private Double discountValue;  // Amount in rupees for FLAT, percentage for PERCENTAGE (e.g., 25 for 25%)

    private Double minOrderAmount;  // Minimum cart total required to apply this coupon (e.g., 500 means min ₹500)

    @Builder.Default
    private Boolean active = true;

    private LocalDateTime validFrom;
    private LocalDateTime validTill;

    public enum CouponType {
        FLAT,       // Flat discount in rupees (e.g., 100 rupees off)
        PERCENTAGE  // Percentage discount (e.g., 25% off)
    }
}
