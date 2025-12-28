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

    @PostMapping("/images/upload/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductImageResponse> uploadProductImage(
            @PathVariable Long productId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "isPrimary", defaultValue = "false") Boolean isPrimary) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mediaService.uploadProductImage(productId, file, isPrimary));
    }

    @GetMapping("/images/{imageId}/download")
    public ResponseEntity<byte[]> downloadProductImage(@PathVariable Long imageId) {
        byte[] imageData = mediaService.downloadProductImage(imageId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=image.jpg")
                .contentType(MediaType.IMAGE_JPEG)
                .body(imageData);
    }

    @DeleteMapping("/images/{imageId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteProductImage(@PathVariable Long imageId) {
        mediaService.deleteProductImage(imageId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Image deleted successfully");
        return ResponseEntity.ok(response);
    }

    // ==================== VIDEO UPLOAD/DELETE ====================

    @PostMapping("/videos/upload/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductVideoResponse> uploadProductVideo(
            @PathVariable Long productId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam(value = "description", defaultValue = "") String description,
            @RequestParam(value = "duration", defaultValue = "0") Integer duration) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mediaService.uploadProductVideo(productId, file, title, description, duration));
    }

    @GetMapping("/videos/{videoId}/download")
    public ResponseEntity<byte[]> downloadProductVideo(@PathVariable Long videoId) {
        byte[] videoData = mediaService.downloadProductVideo(videoId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=video.mp4")
                .contentType(MediaType.valueOf("video/mp4"))
                .body(videoData);
    }

    @DeleteMapping("/videos/{videoId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteProductVideo(@PathVariable Long videoId) {
        mediaService.deleteProductVideo(videoId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Video deleted successfully");
        return ResponseEntity.ok(response);
    }
}
