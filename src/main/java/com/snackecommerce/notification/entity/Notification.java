package com.snackecommerce.notification.entity;

import com.snackecommerce.notification.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_is_read", columnList = "is_read"),
        @Index(name = "idx_type", columnList = "type"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    // Related entity info (e.g., orderId for ORDER_DELIVERED)
    private String relatedEntityType;  // e.g., "ORDER", "SHIPMENT_JOB"
    private Long relatedEntityId;       // e.g., orderId or shipmentJobId

    @Column(name = "is_read")
    private Boolean isRead = false;

    // Additional metadata
    @Column(columnDefinition = "TEXT")
    private String metadata;  // JSON string for extra data (tracking number, delivery time, etc.)

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime readAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}

