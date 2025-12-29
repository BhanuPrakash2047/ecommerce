package com.snackecommerce.cart.controller;

import com.snackecommerce.cart.dto.*;
import com.snackecommerce.cart.service.CartService;
import com.snackecommerce.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "*")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private UserRepository userRepository;

    // ==================== CART MANAGEMENT ====================

    /**
     * GET /api/cart - View current user's cart with all items and alerts
     */
    @GetMapping
    public ResponseEntity<CartResponse> viewCart() {
        Long userId = getCurrentUserId();
        CartResponse cart = cartService.getCart(userId);
        return ResponseEntity.ok(cart);
    }

    /**
     * POST /api/cart/items - Add product to cart
     * Request: { "productId": 1, "quantity": 2 }
     */
    @PostMapping("/items")
    public ResponseEntity<CartResponse> addToCart(@RequestBody AddToCartRequest request) {
        Long userId = getCurrentUserId();
        CartResponse cart = cartService.addToCart(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(cart);
    }

    /**
     * PUT /api/cart/items/{cartItemId} - Update quantity of cart item
     * Request: { "quantity": 5 }
     */
    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<CartResponse> updateQuantity(
            @PathVariable Long cartItemId,
            @RequestBody UpdateQuantityRequest request) {
        Long userId = getCurrentUserId();
        CartResponse cart = cartService.updateQuantity(userId, cartItemId, request.getQuantity());
        return ResponseEntity.ok(cart);
    }

    /**
     * DELETE /api/cart/items/{cartItemId} - Remove item from cart
     */
    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<CartResponse> removeFromCart(@PathVariable Long cartItemId) {
        Long userId = getCurrentUserId();
        CartResponse cart = cartService.removeFromCart(userId, cartItemId);
        return ResponseEntity.ok(cart);
    }

    /**
     * DELETE /api/cart - Clear entire cart
     */
    @DeleteMapping
    public ResponseEntity<Void> clearCart() {
        Long userId = getCurrentUserId();
        // Implementation: cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }

    // ==================== COUPON MANAGEMENT ====================

    /**
     * GET /api/cart/coupons/eligible - Get all available coupons with eligibility status
     * Returns: { "eligibleCoupons": [...], "ineligibleCoupons": [...] }
     */
    @GetMapping("/coupons/eligible")
    public ResponseEntity<EligibleCouponsResponse> getEligibleCoupons() {
        Long userId = getCurrentUserId();
        EligibleCouponsResponse response = cartService.getEligibleCoupons(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/cart/coupons - Apply coupon to cart
     * Request: { "couponId": 5 }
     */
    @PostMapping("/coupons")
    public ResponseEntity<CartResponse> applyCoupon(@RequestBody ApplyCouponRequest request) {
        Long userId = getCurrentUserId();
        CartResponse cart = cartService.applyCoupon(userId, request);
        return ResponseEntity.ok(cart);
    }

    /**
     * DELETE /api/cart/coupons - Remove applied coupon
     */
    @DeleteMapping("/coupons")
    public ResponseEntity<CartResponse> removeCoupon() {
        Long userId = getCurrentUserId();
        CartResponse cart = cartService.removeCoupon(userId);
        return ResponseEntity.ok(cart);
    }

    // ==================== CHECKOUT ====================

    /**
     * POST /api/cart/checkout/validate - Validate cart before checkout
     * Returns: { "isValid": true, "checkoutDetails": {...} }
     * If invalid: { "isValid": false, "issues": [...] }
     */
    @PostMapping("/checkout/validate")
    public ResponseEntity<CheckoutValidationResponse> validateCheckout() {
        Long userId = getCurrentUserId();
        CheckoutValidationResponse response = cartService.validateCheckout(userId);
        
        if (response.getIsValid()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
        }
    }

    /**
     * POST /api/cart/checkout/confirm - Confirm checkout and create order
     * (Dummy implementation - payment integration deferred)
     */
    @PostMapping("/checkout/confirm")
    public ResponseEntity<Map<String, Object>> confirmCheckout(@RequestBody Map<String, Object> request) {
        Long userId = getCurrentUserId();
        
        // Placeholder: In Phase 2, integrate with payment gateway
        // Steps:
        // 1. Validate again
        // 2. Attempt payment
        // 3. Deduct stock
        // 4. Create order
        // 5. Empty cart
        // 6. Return order ID
        
        Map<String, Object> response = new HashMap<>();
        response.put("orderId", "ORDER-" + System.currentTimeMillis());
        response.put("status", "PENDING");
        response.put("message", "Checkout confirmed. Payment integration pending for Phase 2.");
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ==================== HELPER METHODS ====================

    private Long getCurrentUserId() {
        // Extract email from security context (set by JwtAuthenticationFilter)
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        // Look up user by email and return their ID
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email))
                .getId();
    }

    // ==================== INNER CLASS FOR REQUEST ====================

    public static class UpdateQuantityRequest {
        private Integer quantity;

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
    }
}
