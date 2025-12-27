package com.snackecommerce.notification.entity;

import com.snackecommerce.notification.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "notifications")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String message;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    private String relatedEntityType;
    private Long relatedEntityId;

    private Boolean read = false;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}

