package com.snackecommerce.order.entity;

import com.snackecommerce.order.enums.OrderStatus;
import com.snackecommerce.order.enums.TrackingAgent;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String orderNumber;

    private Long userId;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private Double totalAmount;

    // Cart-related fields (added for checkout)
    @Column(name = "subtotal", nullable = true)
    private BigDecimal subtotal;

    @Column(name = "discount_amount", nullable = true)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "total_amount_bigdecimal", nullable = true)
    private BigDecimal totalAmountBigDecimal;

    @Column(name = "applied_coupon_id", nullable = true)
    private Long appliedCouponId;

    @Column(name = "cart_id", nullable = true)
    private Long cartId;

    // Address snapshot
    private String shippingAddress;
    private String phoneNumber;

    // Tracking
    private String trackingNumber;

    @Enumerated(EnumType.STRING)
    private TrackingAgent trackingAgent;

    @Version
    private Long version;  // Optimistic locking for concurrent updates

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = true)
    private LocalDateTime updatedAt;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

