package com.snackecommerce.product.controller;

import com.snackecommerce.product.dto.*;
import com.snackecommerce.product.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/coupons")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class CouponController {

    @Autowired
    private CouponService couponService;

    // ==================== COUPON VALIDATION (PUBLIC) ====================

    @PostMapping("/validate")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CouponValidationResponse> validateCoupon(
            @RequestBody ApplyCouponRequest request) {
        // This will be used by Cart Controller for coupon validation
        // Implementation moved to Cart Service
        return ResponseEntity.ok(null);
    }

    // ==================== ADMIN COUPON MANAGEMENT ====================

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CouponResponse> createCoupon(@RequestBody CouponRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(couponService.createCoupon(request));
    }

    @PutMapping("/{couponId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CouponResponse> updateCoupon(
            @PathVariable Long couponId,
            @RequestBody CouponRequest request) {
        return ResponseEntity.ok(couponService.updateCoupon(couponId, request));
    }

    @DeleteMapping("/{couponId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deactivateCoupon(@PathVariable Long couponId) {
        couponService.deactivateCoupon(couponId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Coupon deactivated successfully");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{couponId}/permanent")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteCoupon(@PathVariable Long couponId) {
        couponService.deleteCoupon(couponId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Coupon and all related links deleted permanently");
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<CouponResponse>> getAllCoupons(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(couponService.getAllCoupons(page, size));
    }

    // ==================== PRODUCT-COUPON LINKING (ADMIN) ====================

    @PostMapping("/{couponId}/products")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> linkProductsToCoupon(
            @PathVariable Long couponId,
            @RequestBody List<Long> productIds) {
        couponService.linkProductsToCoupon(couponId, productIds);
        Map<String, String> response = new HashMap<>();
        response.put("message", productIds.size() + " product(s) linked to coupon successfully");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{couponId}/products/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> unlinkProductFromCoupon(
            @PathVariable Long couponId,
            @PathVariable Long productId) {
        couponService.unlinkProductFromCoupon(couponId, productId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Product unlinked from coupon successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{productId}/active-coupons")
    public ResponseEntity<List<CouponResponse>> getActiveCouponsForProduct(
            @PathVariable Long productId) {
        return ResponseEntity.ok(couponService.getActiveCouponsForProduct(productId));
    }

    @GetMapping("/{couponId}/products")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ProductResponse>> getProductsForCoupon(
            @PathVariable Long couponId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(couponService.getProductsForCoupon(couponId, page, size));
    }
}
