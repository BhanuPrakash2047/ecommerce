package com.snackecommerce.cart.entity;

import com.snackecommerce.cart.enums.CartStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "carts")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Enumerated(EnumType.STRING)
    private CartStatus status;

    private String appliedCouponCode;

    private LocalDateTime updatedAt = LocalDateTime.now();
}

