package com.snackecommerce.product.controller;

import com.snackecommerce.product.dto.*;
import com.snackecommerce.product.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/coupons")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class CouponController {

    @Autowired
    private CouponService couponService;


    // ==================== ADMIN COUPON MANAGEMENT ====================

    /**
     * Create a new coupon (admin only)
     * @param request Coupon creation request
     * @return Created coupon with 201 status
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CouponResponse> createCoupon(@Valid @RequestBody CouponRequest request) {
        CouponResponse coupon = couponService.createCoupon(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(coupon);
    }

    /**
     * Update an existing coupon (admin only)
     * @param couponId Coupon ID
     * @param request Updated coupon request
     * @return Updated coupon or 404 if not found
     */
    @PutMapping("/{couponId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CouponResponse> updateCoupon(
            @PathVariable Long couponId,
            @Valid @RequestBody CouponRequest request) {
        CouponResponse coupon = couponService.updateCoupon(couponId, request);
        return ResponseEntity.ok(coupon);
    }

    /**
     * Deactivate a coupon (soft delete) - admin only
     * @param couponId Coupon ID
     * @return Success message or 404 if not found
     */
    @DeleteMapping("/{couponId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deactivateCoupon(@PathVariable Long couponId) {
        couponService.deactivateCoupon(couponId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Coupon deactivated successfully");
        response.put("status", "success");
        return ResponseEntity.ok(response);
    }

    /**
     * Permanently delete a coupon and all related links (admin only)
     * @param couponId Coupon ID
     * @return Success message or 404 if not found
     */
    @DeleteMapping("/{couponId}/permanent")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteCoupon(@PathVariable Long couponId) {
        couponService.deleteCoupon(couponId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Coupon and all related links deleted permanently");
        response.put("status", "success");
        return ResponseEntity.ok(response);
    }

    /**
     * Get all coupons (admin only)
     * @return List of all coupons
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CouponResponse>> getAllCoupons() {
        List<CouponResponse> coupons = couponService.getAllCoupons();
        return ResponseEntity.ok(coupons);
    }

    /**
     * Get active coupons for a specific product
     * @param productId Product ID
     * @return List of active coupons for the product or 404 if product not found
     */
    @GetMapping("/{productId}/active-coupons")
    public ResponseEntity<List<CouponResponse>> getActiveCouponsForProduct(
            @PathVariable Long productId) {
        List<CouponResponse> coupons = couponService.getActiveCouponsForProduct(productId);
        return ResponseEntity.ok(coupons);
    }

    /**
     * Get all products linked to a coupon (admin only)
     * @param couponId Coupon ID
     * @return List of products or 404 if coupon not found
     */
    @GetMapping("/{couponId}/products")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ProductResponse>> getProductsForCoupon(
            @PathVariable Long couponId) {
        List<ProductResponse> products = couponService.getProductsForCoupon(couponId);
        return ResponseEntity.ok(products);
    }
}
