package com.snackecommerce.order.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_items", indexes = {
        @Index(name = "idx_order_product", columnList = "orderId,productId")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long orderId;

    private Long productId;

    private String productNameSnapshot;

    private BigDecimal unitPriceSnapshot;

    private Integer quantity;

    @Transient
    public BigDecimal getSubtotal() {
        return unitPriceSnapshot.multiply(BigDecimal.valueOf(quantity));
    }

    @Transient
    public BigDecimal getUnitPrice() {
        return unitPriceSnapshot;
    }
}

