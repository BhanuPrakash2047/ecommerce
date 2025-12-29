package com.snackecommerce.cart.entity;

import com.snackecommerce.cart.enums.CartStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "carts")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CartStatus status = CartStatus.ACTIVE;

    // Coupon tracking
    private Long appliedCouponId;  // Store coupon ID instead of code (for updates)
    
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    // Calculated totals (on-the-fly, not persisted)
    @Transient
    private BigDecimal subtotal = BigDecimal.ZERO;
    
    @Transient
    private BigDecimal total = BigDecimal.ZERO;

    // Audit fields
    @Builder.Default
    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Optimistic locking for concurrent checkout prevention
    @Version
    private Long version;
}

