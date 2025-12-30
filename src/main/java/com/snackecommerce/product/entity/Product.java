package com.snackecommerce.product.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "products")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Double price;

    private Double originalPrice;

    private Integer stockQuantity;

    private Integer reservedQuantity = 0;  // Stock reserved for pending payments

    private Boolean active = true;

    private Boolean isEligibleForCoupon = true;  // Controls if product is eligible for coupon systems

    @Version
    private Long version;  // Optimistic locking for concurrent updates

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // No ORM mappings - manual deletion handled in service layer

}
