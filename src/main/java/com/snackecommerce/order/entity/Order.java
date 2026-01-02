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

    // Address snapshot reference - store address ID for checkout address
    @Column(name = "address_id", nullable = true)
    private Long addressId;

    // Receiver details (order-specific)
    private String receiverName;
    private String receiverPhone;
    private String receiverEmail;

    // Tracking
    private String trackingNumber;

    @Enumerated(EnumType.STRING)
    private TrackingAgent trackingAgent;

    // Shipping label URL (download on-the-fly from Delhivery)
    private String shippingLabelUrl;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

