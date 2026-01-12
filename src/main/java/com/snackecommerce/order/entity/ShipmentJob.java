package com.snackecommerce.order.entity;

import com.snackecommerce.order.enums.ShipmentJobStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "shipment_jobs", indexes = {
        @Index(name = "idx_order_id", columnList = "orderId"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_next_retry", columnList = "nextRetryAt")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ShipmentJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShipmentJobStatus status = ShipmentJobStatus.PENDING;

    @Column(nullable = false)
    private Integer attempts = 1;

    @Column(columnDefinition = "TEXT")
    private String lastError;

    private LocalDateTime nextRetryAt;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();

    /**
     * Increment attempt count and set next retry time to 1 minute.
     * Simple linear retry strategy: retry every 1 minute until max attempts reached.
     * Max 5 retries, then marked FAILED.
     */
    public void incrementAttempt(String error) {
        this.attempts++;
        this.lastError = error;
        this.updatedAt = LocalDateTime.now();

        if (this.attempts < 5) {
            // Retry every 1 minute
            this.nextRetryAt = LocalDateTime.now().plusMinutes(1);
        } else {
            // Mark as failed after 5 attempts
            this.status = ShipmentJobStatus.FAILED;
            this.nextRetryAt = null;
        }
    }

    /**
     * Mark job as successfully completed
     */
    public void markSuccess() {
        this.status = ShipmentJobStatus.SUCCESS;
        this.updatedAt = LocalDateTime.now();
        this.nextRetryAt = null;
    }
}
