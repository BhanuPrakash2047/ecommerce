package com.snackecommerce.product.controller;

import com.snackecommerce.product.dto.*;
import com.snackecommerce.product.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private FAQService faqService;

    @Autowired
    private MediaService mediaService;

    // ==================== PRODUCT ENDPOINTS ====================

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(productService.getAllProducts(page, size));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(productService.getProduct(productId));
    }

    @GetMapping("/filter/price")
    public ResponseEntity<Page<ProductResponse>> filterByPrice(
            @RequestParam Double minPrice,
            @RequestParam Double maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(productService.filterProducts(minPrice, maxPrice, page, size));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProductResponse>> searchByName(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(productService.searchByName(name, page, size));
    }

    @GetMapping("/search/advanced")
    public ResponseEntity<Page<ProductResponse>> searchAdvanced(
            @RequestParam String name,
            @RequestParam Double minPrice,
            @RequestParam Double maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(productService.searchByNameAndPrice(name, minPrice, maxPrice, page, size));
    }

    @GetMapping("/active")
    public ResponseEntity<Page<ProductResponse>> getActiveProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(productService.getActiveProducts(page, size));
    }

    @GetMapping("/coupon-eligible")
    public ResponseEntity<Page<ProductResponse>> getCouponEligibleProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(productService.getCouponEligibleProducts(page, size));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> createProduct(@RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(request));
    }

    @PutMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long productId,
            @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(productId, request));
    }

    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteProduct(@PathVariable Long productId) {
        productService.deleteProduct(productId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Product deleted successfully");
        return ResponseEntity.ok(response);
    }

    // ==================== REVIEW ENDPOINTS ====================

    @GetMapping("/{productId}/reviews")
    public ResponseEntity<Page<ReviewResponse>> getProductReviews(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(reviewService.getProductReviews(productId, page, size));
    }

    @PostMapping("/{productId}/reviews")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<ReviewResponse> createReview(
            @PathVariable Long productId,
            @RequestBody ReviewRequest request,
            Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewService.createReview(productId, request, principal.getName()));
    }

    @PutMapping("/reviews/{reviewId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable Long reviewId,
            @RequestBody ReviewRequest request,
            Principal principal) {
        return ResponseEntity.ok(reviewService.updateReview(reviewId, request, principal.getName()));
    }

    @DeleteMapping("/reviews/{reviewId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<Map<String, String>> deleteReview(
            @PathVariable Long reviewId,
            Principal principal) {
        reviewService.deleteReview(reviewId, principal.getName());
        Map<String, String> response = new HashMap<>();
        response.put("message", "Review deleted successfully");
        return ResponseEntity.ok(response);
    }

    // ==================== FAQ ENDPOINTS ====================

    @GetMapping("/{productId}/faqs")
    public ResponseEntity<?> getProductFAQs(@PathVariable Long productId) {
        return ResponseEntity.ok(faqService.getProductFAQs(productId));
    }

    @PostMapping("/{productId}/faqs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FAQResponse> createFAQ(
            @PathVariable Long productId,
            @RequestBody FAQRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(faqService.createFAQ(productId, request));
    }

    @PutMapping("/faqs/{faqId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FAQResponse> updateFAQ(
            @PathVariable Long faqId,
            @RequestBody FAQRequest request) {
        return ResponseEntity.ok(faqService.updateFAQ(faqId, request));
    }

    @DeleteMapping("/faqs/{faqId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteFAQ(@PathVariable Long faqId) {
        faqService.deleteFAQ(faqId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "FAQ deleted successfully");
        return ResponseEntity.ok(response);
    }

    // ==================== IMAGE ENDPOINTS ====================

    @GetMapping("/{productId}/images")
    public ResponseEntity<?> getProductImages(@PathVariable Long productId) {
        return ResponseEntity.ok(mediaService.getProductImages(productId));
    }

    // ==================== VIDEO ENDPOINTS ====================

    @GetMapping("/{productId}/videos")
    public ResponseEntity<?> getProductVideos(@PathVariable Long productId) {
        return ResponseEntity.ok(mediaService.getProductVideos(productId));
    }
}
