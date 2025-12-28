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

    private Integer stockQuantity;

    private Boolean active = true;

    private Boolean isEligibleForCoupon = true;  // Controls if product is eligible for coupon systems

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // No ORM mappings - manual deletion handled in service layer
}
