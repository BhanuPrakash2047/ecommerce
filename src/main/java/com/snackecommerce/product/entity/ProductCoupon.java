package com.snackecommerce.product.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "product_coupons",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"productId", "couponId"}
        )
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ProductCoupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productId;

    private Long couponId;
}
