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
public class ProductVideoResponse {
    private Long id;
    private Long productId;
    private String contentType;
    private Long fileSize;
    private String title;
    private String description;
    private Integer durationInSeconds;
    private Integer displayOrder;
    private LocalDateTime uploadedAt;
    // Note: videoData is NOT included in response for performance
}
