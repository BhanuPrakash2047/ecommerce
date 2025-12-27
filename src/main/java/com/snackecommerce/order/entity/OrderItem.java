package com.snackecommerce.order.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_items")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long orderId;

    private Long productId;

    private String productNameSnapshot;

    private Double unitPriceSnapshot;

    private Integer quantity;

    @Transient
    public Double getSubtotal() {
        return unitPriceSnapshot * quantity;
    }
}

