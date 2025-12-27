package com.snackecommerce.payment.entity;

import com.snackecommerce.payment.enums.PaymentProvider;
import com.snackecommerce.payment.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "payments")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long orderId;

    private Double amount;

    @Enumerated(EnumType.STRING)
    private PaymentProvider provider;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private String providerOrderId;
    private String providerPaymentId;

    @Column(unique = true)
    private String idempotencyKey;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}

