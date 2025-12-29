package com.snackecommerce.cart.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(name = "cart_items", indexes = {
    @Index(name = "idx_cart_product", columnList = "cart_id,product_id"),
    @Index(name = "idx_product", columnList = "product_id")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long cartId;

    @Column(nullable = false)
    private Long productId;

    // Snapshot fields for tracking
    private String productNameSnapshot;  // For display purposes

    @Column(nullable = false)
    private BigDecimal snapshotPrice;    // Price when added/last validated
    
    @Builder.Default
    @Column(nullable = false)
    private Integer quantity = 1;        // 1-999 range

    @Builder.Default
    @Column(updatable = false)
    private LocalDateTime addedAt = LocalDateTime.now();

    @Builder.Default
    private LocalDateTime lastPriceCheckAt = LocalDateTime.now();

    // Transient: Current price (fetched from DB, not stored)
    @Transient
    private BigDecimal currentPrice;

    // Transient: Total for this item (quantity × current price)
    @Transient
    private BigDecimal itemTotal;

    // Transient: Price change alert
    @Transient
    private String priceChangeAlert;
}

