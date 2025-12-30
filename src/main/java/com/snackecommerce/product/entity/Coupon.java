package com.snackecommerce.product.entity;

import com.snackecommerce.product.enums.DiscountType;
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
    private DiscountType discountType;   // PERCENTAGE / FLAT

    private Double discountValue;

    private Double minOrderAmount;

    private Integer maxUsagePerUser;

    private Integer totalUsageLimit;

    @Builder.Default
    private Integer usedCount = 0;

    @Builder.Default
    private Boolean active = true;

    private LocalDateTime validFrom;
    private LocalDateTime validTill;

    @Version
    private Long version;  // Optimistic locking for concurrent usedCount updates


    // No ORM mappings - manual deletion handled in service layer
}
