package com.snackecommerce.product.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_videos")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ProductVideo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Lob
    @Column(columnDefinition = "LONGBLOB", nullable = false)
    private byte[] videoData;  // BLOB - Direct MySQL storage

    private String contentType;  // video/mp4, video/avi, etc.

    private Long fileSize;  // In bytes

    private String title;

    private String description;

    private Integer durationInSeconds;

    private Integer displayOrder = 0;

    @Column(updatable = false)
    private LocalDateTime uploadedAt = LocalDateTime.now();
}
