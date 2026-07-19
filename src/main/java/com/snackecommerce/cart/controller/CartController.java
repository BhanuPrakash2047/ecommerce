package com.snackecommerce.cart.controller;

import com.snackecommerce.common.annotation.RateLimit;
import com.snackecommerce.cart.dto.*;
import com.snackecommerce.cart.service.CartService;
import com.snackecommerce.user.repository.UserRepository;
import com.snackecommerce.user.entity.Address;
import com.snackecommerce.user.repository.AddressRepository;
import com.snackecommerce.user.service.AddressService;
import com.snackecommerce.payment.service.PaymentService;
import com.snackecommerce.payment.dto.CreatePaymentRequest;
import com.snackecommerce.payment.dto.PaymentResponse;
import com.snackecommerce.order.entity.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private static final Logger logger = LoggerFactory.getLogger(CartController.class);

    @Autowired
    private CartService cartService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private AddressService addressService;

    @Autowired
    private PaymentService paymentService;

    // ==================== CART MANAGEMENT ====================

    /**
     * GET /api/cart - View current user's cart with all items and alerts
     */
    @GetMapping
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    @RateLimit(value = "cart", useAuthenticatedUser = true)
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
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    @RateLimit(value = "cart", useAuthenticatedUser = true)
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
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public ResponseEntity<CartResponse> removeFromCart(@PathVariable Long cartItemId) {
        Long userId = getCurrentUserId();
        CartResponse cart = cartService.removeFromCart(userId, cartItemId);
        return ResponseEntity.ok(cart);
    }

    /**
     * DELETE /api/cart - Clear entire cart
     */
    @DeleteMapping
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")

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
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")

    public ResponseEntity<CartResponse> applyCoupon(@RequestBody ApplyCouponRequest request) {
        Long userId = getCurrentUserId();
        CartResponse cart = cartService.applyCoupon(userId, request);
        return ResponseEntity.ok(cart);
    }

    /**
     * DELETE /api/cart/coupons - Remove applied coupon
     */
    @DeleteMapping("/coupons")
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
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
     * POST /api/cart/checkout/confirm - Confirm checkout and initiate payment
     * 
     * IMPORTANT: Call GET /api/cart/validateCheckout first to ensure all conditions are met
     * 
     * Flow:
     * 1. Validate cart (validateCheckout must pass)
     * 2. Create Order with PAYMENT_PENDING status
     * 3. Create OrderItems with price snapshots
     * 4. Create Razorpay order and reserve stock
     * 5. Return Razorpay payment details to frontend
     * 
     * Request: { "email": "userexample.com", "phone": "9876543210" }
     * Response: { "razorpayOrderId": "order_ABC123", "amount": 5000, "orderId": 1, ... }
     */
    @PostMapping("/checkout/confirm")
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public ResponseEntity<?> confirmCheckout(@RequestParam Long id) {
        try {
            Long userId = getCurrentUserId();
            
            // Step 1: Validate checkout (checks all conditions)
            CheckoutValidationResponse validation = cartService.validateCheckout(userId);
            
            if (!validation.getIsValid()) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Checkout validation failed");
                error.put("issues", validation.getIssues());
                error.put("message", validation.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            // Step 2: Get customer and address details from request
            String email = getCurrentUserEmail();
            Long addressId = id;
            String receiverName = "";
            String receiverPhone = "";
            String receiverEmail = "";

            // Validate address exists
            Address address = addressService.requireOwnedAddress(addressId, userId);
            if (address == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Address not found");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            // Get phone number: first from User entity, fallback to Address
            String phone = null;
            var userOptional = userRepository.findByEmail(email);
            if (userOptional.isPresent() && userOptional.get().getPhone() != null && !userOptional.get().getPhone().isEmpty()) {
                phone = userOptional.get().getPhone();
                logger.info("Using phone from user entity: {}", phone);
            } else {
                phone = address.getPhoneNumber();
                logger.info("Using phone from address: {}", phone);
            }
            
            if (email == null || phone == null || addressId == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Email, phone, addressId, receiver name, receiver phone, and receiver email are required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            receiverName = address.getFullName();
            receiverPhone = address.getPhoneNumber();
            receiverEmail = email;

            if ( receiverName == null ||
                    receiverPhone == null ) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Email, phone, addressId, receiver name, receiver phone, and receiver email are required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            // Validate pincode reachability for checkout - always hit Delhivery API directly
            boolean isPincodeReachable = false;
            if (address.getZipCode() != null) {
                var pincodeResponse = addressService.checkPincodeByValue(address.getZipCode());
                isPincodeReachable = pincodeResponse.getIsAvailable();
                logger.info("Delhivery pincode check for {}: isAvailable={}, status={}", 
                           address.getZipCode(), isPincodeReachable, pincodeResponse.getStatus());
            }
            
            if (!isPincodeReachable) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Pincode is not serviceable for delivery");
                error.put("pincode", address.getZipCode());
                error.put("suggestion", "Check pincode reachability using GET /api/address/check-pincode?pincode=" + address.getZipCode());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            // Step 3: Create order from cart with address reference
            Order order = cartService.proceedToCheckout(userId, addressId, receiverName, 
                                                        receiverPhone, receiverEmail);
            
            // Step 4: Create Razorpay order and reserve stock
            CreatePaymentRequest paymentRequest = CreatePaymentRequest.builder()
                    .orderId(order.getId())
                    .amount(order.getTotalAmountBigDecimal().longValue())
                    .email(email)
                    .phone(phone)
                    .build();
            
            PaymentResponse paymentResponse = paymentService.createPayment(paymentRequest);
            
                // Step 5: Return Razorpay details to frontend
            Map<String, Object> response = new HashMap<>();
            response.put("orderId", order.getId());
            response.put("orderNumber", order.getOrderNumber());
            response.put("razorpayOrderId", paymentResponse.getRazorpayOrderId());
            response.put("amount", paymentResponse.getAmount());
            response.put("email", paymentResponse.getEmail());
            response.put("phone", paymentResponse.getPhone());
            response.put("message", "Order created. Ready for payment.");
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (AccessDeniedException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Checkout failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
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
    private String getCurrentUserEmail() {
        // Extract email from security context (set by JwtAuthenticationFilter)
        return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
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
