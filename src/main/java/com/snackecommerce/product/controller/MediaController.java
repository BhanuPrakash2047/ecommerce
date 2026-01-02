package com.snackecommerce.product.controller;

import com.snackecommerce.product.dto.*;
import com.snackecommerce.product.service.MediaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/media")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class MediaController {

    @Autowired
    private MediaService mediaService;

    // ==================== IMAGE UPLOAD/DELETE ====================

    /**
     * Upload a product image (admin only)
     * @param productId Product ID
     * @param file Image file to upload
     * @param isPrimary Mark as primary image
     * @return Uploaded image details with 201 status
     */
    @PostMapping("/images/upload/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductImageResponse> uploadProductImage(
            @PathVariable Long productId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "isPrimary", defaultValue = "false") Boolean isPrimary) throws IOException {
        
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        ProductImageResponse image = mediaService.uploadProductImage(productId, file, isPrimary);
        return ResponseEntity.status(HttpStatus.CREATED).body(image);
    }

    /**
     * Download a product image
     * @param imageId Image ID
     * @return Image data with proper headers
     */
    @GetMapping("/images/{imageId}/download")
    public ResponseEntity<byte[]> downloadProductImage(@PathVariable Long imageId) {
        byte[] imageData = mediaService.downloadProductImage(imageId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=image.jpg")
                .contentType(MediaType.IMAGE_JPEG)
                .body(imageData);
    }

    /**
     * Delete a product image (admin only)
     * @param imageId Image ID
     * @return Success message or 404 if not found
     */
    @DeleteMapping("/images/{imageId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteProductImage(@PathVariable Long imageId) {
        mediaService.deleteProductImage(imageId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Image deleted successfully");
        response.put("status", "success");
        return ResponseEntity.ok(response);
    }

    // ==================== VIDEO UPLOAD/DELETE ====================

    /**
     * Upload a product video (admin only)
     * @param productId Product ID
     * @param file Video file to upload
     * @param title Video title
     * @param description Video description
     * @param duration Video duration in seconds
     * @return Uploaded video details with 201 status
     */
    @PostMapping("/videos/upload/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductVideoResponse> uploadProductVideo(
            @PathVariable Long productId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam(value = "description", defaultValue = "") String description,
            @RequestParam(value = "duration", defaultValue = "0") Integer duration) throws IOException {
        
        if (file.isEmpty() || title == null || title.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        ProductVideoResponse video = mediaService.uploadProductVideo(productId, file, title, description, duration);
        return ResponseEntity.status(HttpStatus.CREATED).body(video);
    }

    /**
     * Download a product video
     * @param videoId Video ID
     * @return Video data with proper headers
     */
    @GetMapping("/videos/{videoId}/download")
    public ResponseEntity<byte[]> downloadProductVideo(@PathVariable Long videoId) {
        byte[] videoData = mediaService.downloadProductVideo(videoId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=video.mp4")
                .contentType(MediaType.valueOf("video/mp4"))
                .body(videoData);
    }

    /**
     * Delete a product video (admin only)
     * @param videoId Video ID
     * @return Success message or 404 if not found
     */
    @DeleteMapping("/videos/{videoId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteProductVideo(@PathVariable Long videoId) {
        mediaService.deleteProductVideo(videoId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Video deleted successfully");
        response.put("status", "success");
        return ResponseEntity.ok(response);
    }
}
