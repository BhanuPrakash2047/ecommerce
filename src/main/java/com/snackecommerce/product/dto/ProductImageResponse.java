package com.snackecommerce.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImageResponse {
    private Long id;
    private Long productId;
    private String contentType;
    private Long fileSize;
    private Integer displayOrder;
    private Boolean isPrimary;
    private LocalDateTime uploadedAt;
    // Note: imageData is NOT included in response for performance (use separate endpoint if needed)
}
