package com.snackecommerce.order.entity;

import com.snackecommerce.order.enums.OrderStatus;
import com.snackecommerce.order.enums.TrackingAgent;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String orderNumber;

    private Long userId;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private Double totalAmount;

    // Address snapshot
    private String shippingAddress;
    private String phoneNumber;

    // Tracking
    private String trackingNumber;

    @Enumerated(EnumType.STRING)
    private TrackingAgent trackingAgent;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}

