package com.snackecommerce.product.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_images")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Lob
    @Column(columnDefinition = "LONGBLOB", nullable = false)
    private byte[] imageData;  // BLOB - Direct MySQL storage

    private String contentType;  // image/jpeg, image/png, etc.

    private Long fileSize;  // In bytes

    private Integer displayOrder = 0;  // For ordering images

    private Boolean isPrimary = false;  // Main product image

    @Column(updatable = false)
    private LocalDateTime uploadedAt = LocalDateTime.now();
}
