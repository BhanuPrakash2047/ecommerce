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

    private Boolean isAvailable = true;  // Simple availability flag - no stock limits

    private Boolean isEligibleForCoupon = true;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // No ORM mappings - manual deletion handled in service layer

}
