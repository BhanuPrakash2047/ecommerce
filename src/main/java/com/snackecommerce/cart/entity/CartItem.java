package com.snackecommerce.cart.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "cart_items")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long cartId;

    private Long productId;

    private String productNameSnapshot;

    private Double unitPriceSnapshot;

    private Integer quantity;

    private LocalDateTime addedAt = LocalDateTime.now();

    @Transient
    public Double getSubtotal() {
        return unitPriceSnapshot * quantity;
    }
}

