package com.snackecommerce.product.service;

import com.snackecommerce.common.exception.InvalidFileException;
import com.snackecommerce.common.exception.ProductNotFoundException;
import com.snackecommerce.product.dto.ProductImageResponse;
import com.snackecommerce.product.dto.ProductVideoResponse;
import com.snackecommerce.product.entity.Product;
import com.snackecommerce.product.entity.ProductImage;
import com.snackecommerce.product.entity.ProductVideo;
import com.snackecommerce.product.repository.ProductImageRepository;
import com.snackecommerce.product.repository.ProductRepository;
import com.snackecommerce.product.repository.ProductVideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class MediaService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    @Autowired
    private ProductVideoRepository productVideoRepository;

    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024;  // 10 MB
    private static final long MAX_VIDEO_SIZE = 100 * 1024 * 1024;  // 100 MB
    private static final String[] ALLOWED_IMAGE_TYPES = {"image/jpeg", "image/png", "image/webp"};
    private static final String[] ALLOWED_VIDEO_TYPES = {"video/mp4", "video/avi", "video/quicktime"};

    // ==================== IMAGE OPERATIONS ====================

    public ProductImageResponse uploadProductImage(Long productId, MultipartFile file, Boolean isPrimary) throws IOException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));

        // Validate image file
//        if (file.getSize() > MAX_IMAGE_SIZE) {
//            throw new InvalidFileException("Image size exceeds maximum limit of 5 MB");
//        }

        if (!isValidImageType(file.getContentType())) {
            throw new InvalidFileException("Invalid image format. Allowed: JPEG, PNG, WebP");
        }

        // If setting as primary, remove other primary images
        if (isPrimary != null && isPrimary) {
            List<ProductImage> images = productImageRepository.findByProductIdOrderByDisplayOrder(productId);
            images.forEach(img -> img.setIsPrimary(false));
            productImageRepository.saveAll(images);
        }

        ProductImage image = ProductImage.builder()
                .product(product)
                .imageData(file.getBytes())
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .isPrimary(isPrimary != null && isPrimary)
                .displayOrder(0)
                .build();

        image = productImageRepository.save(image);
        return mapImageToResponse(image);
    }

    public List<ProductImageResponse> getProductImages(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ProductNotFoundException("Product not found with ID: " + productId);
        }

        return productImageRepository.findByProductIdOrderByDisplayOrder(productId).stream()
                .map(this::mapImageToResponse)
                .collect(Collectors.toList());
    }

    public byte[] downloadProductImage(Long imageId) {
        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found with ID: " + imageId));
        return image.getImageData();
    }

    public void deleteProductImage(Long imageId) {
        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found with ID: " + imageId));
        productImageRepository.delete(image);
    }

    // ==================== VIDEO OPERATIONS ====================

    public ProductVideoResponse uploadProductVideo(Long productId, MultipartFile file, String title, String description, Integer duration) throws IOException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));

        // Validate video file
        if (file.getSize() > MAX_VIDEO_SIZE) {
            throw new InvalidFileException("Video size exceeds maximum limit of 100 MB");
        }

        if (!isValidVideoType(file.getContentType())) {
            throw new InvalidFileException("Invalid video format. Allowed: MP4, AVI, MOV");
        }

        ProductVideo video = ProductVideo.builder()
                .product(product)
                .videoData(file.getBytes())
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .title(title)
                .description(description)
                .durationInSeconds(duration)
                .displayOrder(0)
                .build();

        video = productVideoRepository.save(video);
        return mapVideoToResponse(video);
    }

    public List<ProductVideoResponse> getProductVideos(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ProductNotFoundException("Product not found with ID: " + productId);
        }

        return productVideoRepository.findByProductIdOrderByDisplayOrder(productId).stream()
                .map(this::mapVideoToResponse)
                .collect(Collectors.toList());
    }

    public byte[] downloadProductVideo(Long videoId) {
        ProductVideo video = productVideoRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found with ID: " + videoId));
        return video.getVideoData();
    }

    public void deleteProductVideo(Long videoId) {
        ProductVideo video = productVideoRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found with ID: " + videoId));
        productVideoRepository.delete(video);
    }

    // ==================== HELPER METHODS ====================

    private boolean isValidImageType(String contentType) {
        if (contentType == null) return false;
        for (String type : ALLOWED_IMAGE_TYPES) {
            if (contentType.equals(type)) return true;
        }
        return false;
    }

    private boolean isValidVideoType(String contentType) {
        if (contentType == null) return false;
        for (String type : ALLOWED_VIDEO_TYPES) {
            if (contentType.equals(type)) return true;
        }
        return false;
    }

    private ProductImageResponse mapImageToResponse(ProductImage image) {
        return ProductImageResponse.builder()
                .id(image.getId())
                .productId(image.getProduct().getId())
                .contentType(image.getContentType())
                .fileSize(image.getFileSize())
                .displayOrder(image.getDisplayOrder())
                .isPrimary(image.getIsPrimary())
                .uploadedAt(image.getUploadedAt())
                .build();
    }

    private ProductVideoResponse mapVideoToResponse(ProductVideo video) {
        return ProductVideoResponse.builder()
                .id(video.getId())
                .productId(video.getProduct().getId())
                .contentType(video.getContentType())
                .fileSize(video.getFileSize())
                .title(video.getTitle())
                .description(video.getDescription())
                .durationInSeconds(video.getDurationInSeconds())
                .displayOrder(video.getDisplayOrder())
                .uploadedAt(video.getUploadedAt())
                .build();
    }
}
