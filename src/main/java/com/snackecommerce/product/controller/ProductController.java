package com.snackecommerce.product.controller;

import com.snackecommerce.product.dto.*;
import com.snackecommerce.product.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
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

    /**
     * Get all products with pagination
     * @return List of all products
     */
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<ProductResponse> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    /**
     * Get a specific product by ID
     * @param productId Product ID
     * @return Product details or 404 if not found
     */
    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable Long productId) {
        ProductResponse product = productService.getProduct(productId);
        return ResponseEntity.ok(product);
    }

    /**
     * Filter products by price range
     * @param minPrice Minimum price
     * @param maxPrice Maximum price
     * @return List of products within price range
     */
    @GetMapping("/filter/price")
    public ResponseEntity<List<ProductResponse>> filterByPrice(
            @RequestParam(required = true) Double minPrice,
            @RequestParam(required = true) Double maxPrice) {
        
        if (minPrice < 0 || maxPrice < 0 || minPrice > maxPrice) {
            return ResponseEntity.badRequest().build();
        }
        
        List<ProductResponse> products = productService.filterProducts(minPrice, maxPrice, 0, 10);
        return ResponseEntity.ok(products);
    }

    /**
     * Search products by name
     * @param name Product name or keyword
     * @return List of matching products
     */
    @GetMapping("/search")
    public ResponseEntity<List<ProductResponse>> searchByName(
            @RequestParam(required = true) String name) {
        
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        List<ProductResponse> products = productService.searchByName(name, 0, 10);
        return ResponseEntity.ok(products);
    }

    /**
     * Advanced search with name and price filters
     * @param name Product name
     * @param minPrice Minimum price
     * @param maxPrice Maximum price
     * @return List of matching products
     */
    @GetMapping("/search/advanced")
    public ResponseEntity<List<ProductResponse>> searchAdvanced(
            @RequestParam(required = true) String name,
            @RequestParam(required = true) Double minPrice,
            @RequestParam(required = true) Double maxPrice) {
        
        if (name == null || name.trim().isEmpty() || minPrice < 0 || maxPrice < 0 || minPrice > maxPrice) {
            return ResponseEntity.badRequest().build();
        }
        
        List<ProductResponse> products = productService.searchByNameAndPrice(name, minPrice, maxPrice, 0, 10);
        return ResponseEntity.ok(products);
    }

    /**
     * Get all available products (in stock)
     * @return List of available products
     */
    @GetMapping("/available")
    public ResponseEntity<List<ProductResponse>> getAvailableProducts() {
        List<ProductResponse> products = productService.getAvailableProducts();
        return ResponseEntity.ok(products);
    }

    /**
     * Get products eligible for coupons
     * @return List of coupon-eligible products
     */
    @GetMapping("/coupon-eligible")
    public ResponseEntity<List<ProductResponse>> getCouponEligibleProducts() {
        List<ProductResponse> products = productService.getCouponEligibleProducts();
        return ResponseEntity.ok(products);
    }

    /**
     * Create a new product (admin only)
     * @param request Product creation request
     * @return Created product with 201 status
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        ProductResponse product = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    /**
     * Update an existing product (admin only)
     * @param productId Product ID
     * @param request Product update request
     * @return Updated product or 404 if not found
     */
    @PutMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long productId,
            @Valid @RequestBody ProductRequest request) {
        ProductResponse product = productService.updateProduct(productId, request);
        return ResponseEntity.ok(product);
    }

    /**
     * Delete a product (admin only)
     * @param productId Product ID
     * @return Success message or 404 if not found
     */
    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteProduct(@PathVariable Long productId) {
        productService.deleteProduct(productId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Product deleted successfully");
        response.put("status", "success");
        return ResponseEntity.ok(response);
    }

    // ==================== REVIEW ENDPOINTS ====================

    /**
     * Get reviews for a product
     * @param productId Product ID
     * @param page Page number (default 0)
     * @param size Page size (default 10)
     * @return Paginated reviews or 404 if product not found
     */
    @GetMapping("/{productId}/reviews")
    public ResponseEntity<Page<ReviewResponse>> getProductReviews(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        if (page < 0 || size < 1 || size > 100) {
            return ResponseEntity.badRequest().build();
        }
        
        Page<ReviewResponse> reviews = reviewService.getProductReviews(productId, page, size);
        return ResponseEntity.ok(reviews);
    }

    /**
     * Create a review for a product
     * @param productId Product ID
     * @param request Review request
     * @param principal Authenticated user
     * @return Created review with 201 status
     */
    @PostMapping("/{productId}/reviews")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ReviewResponse> createReview(
            @PathVariable Long productId,
            @Valid @RequestBody ReviewRequest request,
            Principal principal) {
        ReviewResponse review = reviewService.createReview(productId, request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(review);
    }

    /**
     * Update a review (only by review author or admin)
     * @param reviewId Review ID
     * @param request Updated review request
     * @param principal Authenticated user
     * @return Updated review
     */
    @PutMapping("/reviews/{reviewId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewRequest request,
            Principal principal) {
        ReviewResponse review = reviewService.updateReview(reviewId, request, principal.getName());
        return ResponseEntity.ok(review);
    }

    /**
     * Delete a review (only by review author or admin)
     * @param reviewId Review ID
     * @param principal Authenticated user
     * @return Success message
     */
    @DeleteMapping("/reviews/{reviewId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteReview(
            @PathVariable Long reviewId,
            Principal principal) {
        reviewService.deleteReview(reviewId, principal.getName());
        Map<String, String> response = new HashMap<>();
        response.put("message", "Review deleted successfully");
        response.put("status", "success");
        return ResponseEntity.ok(response);
    }

    // ==================== FAQ ENDPOINTS ====================

    /**
     * Get FAQs for a product
     * @param productId Product ID
     * @return List of FAQs or 404 if product not found
     */
    @GetMapping("/{productId}/faqs")
    public ResponseEntity<?> getProductFAQs(@PathVariable Long productId) {
        var faqs = faqService.getProductFAQs(productId);
        return ResponseEntity.ok(faqs);
    }

    /**
     * Create a FAQ for a product (admin only)
     * @param productId Product ID
     * @param request FAQ request
     * @return Created FAQ with 201 status
     */
    @PostMapping("/{productId}/faqs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FAQResponse> createFAQ(
            @PathVariable Long productId,
            @Valid @RequestBody FAQRequest request) {
        FAQResponse faq = faqService.createFAQ(productId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(faq);
    }

    /**
     * Update a FAQ (admin only)
     * @param faqId FAQ ID
     * @param request Updated FAQ request
     * @return Updated FAQ or 404 if not found
     */
    @PutMapping("/faqs/{faqId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FAQResponse> updateFAQ(
            @PathVariable Long faqId,
            @Valid @RequestBody FAQRequest request) {
        FAQResponse faq = faqService.updateFAQ(faqId, request);
        return ResponseEntity.ok(faq);
    }

    /**
     * Delete a FAQ (admin only)
     * @param faqId FAQ ID
     * @return Success message or 404 if not found
     */
    @DeleteMapping("/faqs/{faqId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteFAQ(@PathVariable Long faqId) {
        faqService.deleteFAQ(faqId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "FAQ deleted successfully");
        response.put("status", "success");
        return ResponseEntity.ok(response);
    }

    // ==================== IMAGE ENDPOINTS ====================

    /**
     * Get all images for a product
     * @param productId Product ID
     * @return List of product images or 404 if product not found
     */
    @GetMapping("/{productId}/images")
    public ResponseEntity<?> getProductImages(@PathVariable Long productId) {
        var images = mediaService.getProductImages(productId);
        return ResponseEntity.ok(images);
    }

    // ==================== VIDEO ENDPOINTS ====================

    /**
     * Get all videos for a product
     * @param productId Product ID
     * @return List of product videos or 404 if product not found
     */
    @GetMapping("/{productId}/videos")
    public ResponseEntity<?> getProductVideos(@PathVariable Long productId) {
        var videos = mediaService.getProductVideos(productId);
        return ResponseEntity.ok(videos);
    }
}
