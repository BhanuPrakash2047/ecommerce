package com.snackecommerce.cart.service;

import com.snackecommerce.cart.dto.*;
import com.snackecommerce.cart.entity.Cart;
import com.snackecommerce.cart.entity.CartItem;
import com.snackecommerce.cart.enums.CartStatus;
import com.snackecommerce.cart.repository.CartItemRepository;
import com.snackecommerce.cart.repository.CartRepository;
import com.snackecommerce.common.exception.*;
import com.snackecommerce.product.entity.Coupon;
import com.snackecommerce.product.entity.Product;
import com.snackecommerce.product.repository.CouponRepository;
import com.snackecommerce.product.repository.ProductCouponRepository;
import com.snackecommerce.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private ProductCouponRepository productCouponRepository;

    // ==================== 1. GET OR CREATE CART ====================

    public Cart getOrCreateCart(Long userId) {
        Optional<Cart> existingCart = cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE);
        if (existingCart.isPresent()) {
            return existingCart.get();
        }
        
        Cart newCart = Cart.builder()
                .userId(userId)
                .status(CartStatus.ACTIVE)
                .discountAmount(BigDecimal.ZERO)
                .build();
        return cartRepository.save(newCart);
    }

    // ==================== 2. ADD TO CART ====================

    public CartResponse addToCart(Long userId, AddToCartRequest request) {
        // Validate quantity
        if (request.getQuantity() < 1 || request.getQuantity() > 100) {
            throw new InvalidCartItemException("Quantity must be between 1 and 100");
        }

        // Get cart
        Cart cart = getOrCreateCart(userId);

        // Check product exists
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + request.getProductId()));

        // Check stock availability
        if (product.getStockQuantity() < request.getQuantity()) {
            throw new InsufficientStockException("Only " + product.getStockQuantity() + " units available for " + product.getName());
        }

        // Get current price
        BigDecimal currentPrice = BigDecimal.valueOf(product.getPrice());

        // Check if product already in cart (prevent duplicates - merge instead)
        Optional<CartItem> existingItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), request.getProductId());
        
        CartItem cartItem;
        List<String> alerts = new ArrayList<>();

        if (existingItem.isPresent()) {
            // Item exists: merge quantities
            cartItem = existingItem.get();
            Integer newQuantity = cartItem.getQuantity() + request.getQuantity();
            
            // Check stock for merged quantity
            if (product.getStockQuantity() < newQuantity) {
                throw new InsufficientStockException("Only " + product.getStockQuantity() + " units available. Current in cart: " + cartItem.getQuantity());
            }

            // Check for price changes
            if (!cartItem.getSnapshotPrice().equals(currentPrice)) {
                BigDecimal priceDiff = currentPrice.subtract(cartItem.getSnapshotPrice());
                String direction = priceDiff.compareTo(BigDecimal.ZERO) > 0 ? "↑" : "↓";
                alerts.add("Price " + direction + " ₹" + cartItem.getSnapshotPrice() + " → ₹" + currentPrice + " (" + priceDiff.abs() + ")");
                cartItem.setSnapshotPrice(currentPrice);
            }

            cartItem.setQuantity(newQuantity);
        } else {
            // New item
            cartItem = CartItem.builder()
                    .cartId(cart.getId())
                    .productId(request.getProductId())
                    .productNameSnapshot(product.getName())
                    .snapshotPrice(currentPrice)
                    .quantity(request.getQuantity())
                    .lastPriceCheckAt(LocalDateTime.now())
                    .build();
        }

        cartItem.setLastPriceCheckAt(LocalDateTime.now());
        cartItemRepository.save(cartItem);

        // Update cart timestamp
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        // Recalculate and return
        return getCartResponse(cart, alerts);
    }

    // ==================== 3. REMOVE FROM CART ====================
    public CartResponse removeFromCart(Long userId, Long cartItemId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found for user: " + userId));

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new InvalidCartItemException("Cart item not found: " + cartItemId));

        if (!item.getCartId().equals(cart.getId())) {
            throw new InvalidCartItemException("Item doesn't belong to this cart");
        }

        cartItemRepository.delete(item);
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        List<String> alerts = new ArrayList<>();
        // Check if coupon still valid after item removal
        if (cart.getAppliedCouponId() != null) {
            if (!isCouponStillValidForCart(cart)) {
                alerts.add("Applied coupon no longer valid for remaining items. Discount removed.");
                cart.setAppliedCouponId(null);
                cart.setDiscountAmount(BigDecimal.ZERO);
                cartRepository.save(cart);
            }
        }

        return getCartResponse(cart, alerts);
    }

    // ==================== 4. UPDATE QUANTITY ====================

    public CartResponse updateQuantity(Long userId, Long cartItemId, Integer newQuantity) {
        // Validate quantity
        if (newQuantity < 1 || newQuantity > 999) {
            throw new InvalidCartItemException("Quantity must be between 1 and 999");
        }

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found for user: " + userId));

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new InvalidCartItemException("Cart item not found: " + cartItemId));

        if (!item.getCartId().equals(cart.getId())) {
            throw new InvalidCartItemException("Item doesn't belong to this cart");
        }

        Product product = productRepository.findById(item.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        // Check stock for new quantity
        if (product.getStockQuantity() < newQuantity) {
            throw new InsufficientStockException("Only " + product.getStockQuantity() + " units available");
        }

        // Check for price changes
        BigDecimal currentPrice = BigDecimal.valueOf(product.getPrice());
        List<String> alerts = new ArrayList<>();

        if (!item.getSnapshotPrice().equals(currentPrice)) {
            BigDecimal priceDiff = currentPrice.subtract(item.getSnapshotPrice());
            String direction = priceDiff.compareTo(BigDecimal.ZERO) > 0 ? "↑" : "↓";
            alerts.add("Price " + direction + " ₹" + item.getSnapshotPrice() + " → ₹" + currentPrice);
            item.setSnapshotPrice(currentPrice);
        }

        item.setQuantity(newQuantity);
        item.setLastPriceCheckAt(LocalDateTime.now());
        cartItemRepository.save(item);

        // Recalculate coupon eligibility after quantity change
        if (cart.getAppliedCouponId() != null) {
            if (!isCouponStillValidForCart(cart)) {
                alerts.add("Applied coupon no longer valid after quantity update. Discount removed.");
                cart.setAppliedCouponId(null);
                cart.setDiscountAmount(BigDecimal.ZERO);
            }
        }

        // Recalculate everything including coupon
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        return getCartResponse(cart, alerts);
    }

    // ==================== 5. VIEW CART ====================
    public CartResponse getCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found for user: " + userId));

        List<String> alerts = new ArrayList<>();
        
        // Validate all products and prices
        validateAllCartItems(cart, alerts);
        
        // Check if applied coupon still valid
        if (cart.getAppliedCouponId() != null) {
            validateAppliedCoupon(cart, alerts);
        }

        return getCartResponse(cart, alerts);
    }

    // ==================== 6. GET ELIGIBLE COUPONS ====================

    public EligibleCouponsResponse getEligibleCoupons(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found for user: " + userId));

        List<EligibleCouponsResponse.CouponOption> eligible = new ArrayList<>();
        List<EligibleCouponsResponse.CouponOption> ineligible = new ArrayList<>();

        List<Coupon> allCoupons = couponRepository.findAll();

        for (Coupon coupon : allCoupons) {
            EligibleCouponsResponse.CouponOption option = evaluateCouponForCart(coupon, cart);
            
            if (option.getIsEligible()) {
                eligible.add(option);
            } else {
                ineligible.add(option);
            }
        }

        return EligibleCouponsResponse.builder()
                .eligibleCoupons(eligible)
                .ineligibleCoupons(ineligible)
                .build();
    }

    // ==================== 7. APPLY COUPON ====================

    public CartResponse applyCoupon(Long userId, ApplyCouponRequest request) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found for user: " + userId));

        Coupon coupon = couponRepository.findById(request.getCouponId())
                .orElseThrow(() -> new CouponNotFoundException("Coupon not found: " + request.getCouponId()));

        // Validate coupon
        String validationError = validateCouponForApply(coupon, cart);
        if (validationError != null) {
            throw new InvalidCouponStateException(validationError);
        }

        // Apply coupon
        cart.setAppliedCouponId(coupon.getId());
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        List<String> alerts = new ArrayList<>();
        alerts.add("Coupon applied: " + coupon.getCode());

        return getCartResponse(cart, alerts);
    }

    // ==================== 8. REMOVE COUPON ====================

    public CartResponse removeCoupon(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found for user: " + userId));

        cart.setAppliedCouponId(null);
        cart.setDiscountAmount(BigDecimal.ZERO);
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        List<String> alerts = new ArrayList<>();
        alerts.add("Coupon removed");

        return getCartResponse(cart, alerts);
    }

    // ==================== 14. FINAL CHECKOUT VALIDATION ====================

    public CheckoutValidationResponse validateCheckout(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found for user: " + userId));

        List<String> issues = new ArrayList<>();

        // Check 1: Cart not empty
        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());
        if (items.isEmpty()) {
            return CheckoutValidationResponse.builder()
                    .isValid(false)
                    .message("Cart is empty")
                    .issues(List.of("Add products to cart before checkout"))
                    .build();
        }

        // For each item, validate
        List<CartItemResponse> validatedItems = new ArrayList<>();

        for (CartItem item : items) {
            Product product = productRepository.findById(item.getProductId()).orElse(null);

            // Check: Product exists
            if (product == null) {
                issues.add("Product not found. Removed from cart");
                cartItemRepository.delete(item);
                continue;
            }

            // Check: Stock sufficient
            if (product.getStockQuantity() < item.getQuantity()) {
                issues.add("Insufficient stock for " + product.getName() + ". Available: " + product.getStockQuantity());
                cartItemRepository.delete(item);
                continue;
            }

            // Check: Price matches snapshot
            BigDecimal currentPrice = BigDecimal.valueOf(product.getPrice());
            if (!currentPrice.equals(item.getSnapshotPrice())) {
                issues.add("Price changed for " + product.getName() + ": ₹" + item.getSnapshotPrice() + " → ₹" + currentPrice);
                cartItemRepository.delete(item);
                continue;
            }

            // Valid item
            CartItemResponse itemResponse = CartItemResponse.builder()
                    .cartItemId(item.getId())
                    .productId(item.getProductId())
                    .productName(item.getProductNameSnapshot())
                    .quantity(item.getQuantity())
                    .snapshotPrice(item.getSnapshotPrice())
                    .currentPrice(currentPrice)
                    .itemTotal(currentPrice.multiply(BigDecimal.valueOf(item.getQuantity())))
                    .build();

            validatedItems.add(itemResponse);
        }

        // If no valid items remain
        if (validatedItems.isEmpty()) {
            return CheckoutValidationResponse.builder()
                    .isValid(false)
                    .message("All items became invalid")
                    .issues(issues)
                    .build();
        }

        // Validate applied coupon
        if (cart.getAppliedCouponId() != null) {
            Coupon coupon = couponRepository.findById(cart.getAppliedCouponId()).orElse(null);

            if (coupon == null || !coupon.getActive() || coupon.getValidTill().isBefore(LocalDateTime.now())) {
                issues.add("Applied coupon is no longer valid");
                cart.setAppliedCouponId(null);
                cart.setDiscountAmount(BigDecimal.ZERO);
                cartRepository.save(cart);
            } else {
                // Verify coupon still applies to items AND eligible total meets minimum (CRITICAL BUG FIX)
                BigDecimal eligibleTotal = BigDecimal.ZERO;
                for (CartItemResponse itemResp : validatedItems) {
                    if (productCouponRepository.findByProductIdAndCouponId(itemResp.getProductId(), coupon.getId()).isPresent()) {
                        eligibleTotal = eligibleTotal.add(itemResp.getItemTotal());
                    }
                }

                // Check 1: Has eligible products
                if (eligibleTotal.compareTo(BigDecimal.ZERO) == 0) {
                    issues.add("No products in cart are eligible for applied coupon");
                    cart.setAppliedCouponId(null);
                    cart.setDiscountAmount(BigDecimal.ZERO);
                    cartRepository.save(cart);
                }
                // Check 2: Eligible total meets minimum requirement
                else if (eligibleTotal.compareTo(BigDecimal.valueOf(coupon.getMinOrderAmount())) < 0) {
                    issues.add("Eligible products total (₹" + eligibleTotal + ") below minimum (₹" + coupon.getMinOrderAmount() + ")");
                    cart.setAppliedCouponId(null);
                    cart.setDiscountAmount(BigDecimal.ZERO);
                    cartRepository.save(cart);
                }
            }
        }

        // Calculate final totals
        BigDecimal subtotal = validatedItems.stream()
                .map(CartItemResponse::getItemTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discount = BigDecimal.ZERO;
        if (cart.getAppliedCouponId() != null) {
            discount = cart.getDiscountAmount();
        }

        BigDecimal finalTotal = subtotal.subtract(discount);

        if (!issues.isEmpty()) {
            return CheckoutValidationResponse.builder()
                    .isValid(false)
                    .message("Checkout validation failed. Please review your cart.")
                    .issues(issues)
                    .build();
        }

        // All checks passed
        return CheckoutValidationResponse.builder()
                .isValid(true)
                .message("Checkout validation successful")
                .checkoutDetails(CheckoutValidationResponse.CheckoutDetails.builder()
                        .totalItems(validatedItems.size())
                        .subtotal(subtotal)
                        .discountAmount(discount)
                        .appliedCouponId(cart.getAppliedCouponId())
                        .finalTotal(finalTotal)
                        .items(validatedItems)
                        .build())
                .build();
    }

    // ==================== HELPER METHODS ====================
    private CartResponse getCartResponse(Cart cart, List<String> alerts) {
        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());
        List<CartItemResponse> itemResponses = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (CartItem item : items) {
            Product product = productRepository.findById(item.getProductId()).orElse(null);

            if (product == null) {
                // Product deleted - remove from cart
                cartItemRepository.delete(item);
                alerts.add("Product no longer available. Removed from cart");
                continue;
            }

            BigDecimal currentPrice = BigDecimal.valueOf(product.getPrice());
            BigDecimal itemTotal = currentPrice.multiply(BigDecimal.valueOf(item.getQuantity()));

            // Check price changes
            String priceAlert = null;
            if (!currentPrice.equals(item.getSnapshotPrice())) {
                BigDecimal diff = currentPrice.subtract(item.getSnapshotPrice());
                String direction = diff.compareTo(BigDecimal.ZERO) > 0 ? "↑" : "↓";
                priceAlert = "Price " + direction + " ₹" + item.getSnapshotPrice() + " → ₹" + currentPrice;
                item.setSnapshotPrice(currentPrice);
                item.setLastPriceCheckAt(LocalDateTime.now());
                cartItemRepository.save(item);
                alerts.add(priceAlert);
            }

            itemResponses.add(CartItemResponse.builder()
                    .cartItemId(item.getId())
                    .productId(item.getProductId())
                    .productName(item.getProductNameSnapshot())
                    .quantity(item.getQuantity())
                    .snapshotPrice(item.getSnapshotPrice())
                    .currentPrice(currentPrice)
                    .itemTotal(itemTotal)
                    .priceChangeAlert(priceAlert)
                    .build());

            subtotal = subtotal.add(itemTotal);
        }

        BigDecimal discount = cart.getDiscountAmount() != null ? cart.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal total = subtotal.subtract(discount);

        return CartResponse.builder()
                .cartId(cart.getId())
                .userId(cart.getUserId())
                .items(itemResponses)
                .subtotal(subtotal)
                .discountAmount(discount)
                .total(total)
                .appliedCouponId(cart.getAppliedCouponId())
                .alerts(alerts)
                .build();
    }

    private void validateAllCartItems(Cart cart, List<String> alerts) {
        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());

        for (CartItem item : items) {
            Product product = productRepository.findById(item.getProductId()).orElse(null);

            // Check: Product exists
            if (product == null) {
                alerts.add("Product no longer available. Removing from cart");
                cartItemRepository.delete(item);
                continue;
            }

            // Check: Stock
            if (product.getStockQuantity() < item.getQuantity()) {
                alerts.add(product.getName() + ": Only " + product.getStockQuantity() + " available. Quantity reduced.");
                item.setQuantity(Math.min(item.getQuantity(), product.getStockQuantity()));
                cartItemRepository.save(item);
            }

            // Check: Price
            BigDecimal currentPrice = BigDecimal.valueOf(product.getPrice());
            if (!currentPrice.equals(item.getSnapshotPrice())) {
                BigDecimal diff = currentPrice.subtract(item.getSnapshotPrice());
                String direction = diff.compareTo(BigDecimal.ZERO) > 0 ? "↑" : "↓";
                alerts.add(product.getName() + ": Price " + direction + " ₹" + item.getSnapshotPrice() + " → ₹" + currentPrice);
                item.setSnapshotPrice(currentPrice);
                item.setLastPriceCheckAt(LocalDateTime.now());
                cartItemRepository.save(item);
            }
        }
    }

    private void validateAppliedCoupon(Cart cart, List<String> alerts) {
        Coupon coupon = couponRepository.findById(cart.getAppliedCouponId()).orElse(null);

        if (coupon == null) {
            alerts.add("Applied coupon not found. Removed.");
            cart.setAppliedCouponId(null);
            cart.setDiscountAmount(BigDecimal.ZERO);
            cartRepository.save(cart);
            return;
        }

        // Check: Expired
        if (coupon.getValidTill().isBefore(LocalDateTime.now())) {
            alerts.add("Applied coupon expired. Discount removed.");
            cart.setAppliedCouponId(null);
            cart.setDiscountAmount(BigDecimal.ZERO);
            cartRepository.save(cart);
            return;
        }

        // Check: Active
        if (!coupon.getActive()) {
            alerts.add("Applied coupon no longer active. Discount removed.");
            cart.setAppliedCouponId(null);
            cart.setDiscountAmount(BigDecimal.ZERO);
            cartRepository.save(cart);
            return;
        }

        // Check: Products still eligible AND eligible total meets minimum
        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());
        BigDecimal eligibleTotal = BigDecimal.ZERO;
        
        for (CartItem item : items) {
            if (productCouponRepository.findByProductIdAndCouponId(item.getProductId(), coupon.getId()).isPresent()) {
                Product product = productRepository.findById(item.getProductId()).orElse(null);
                if (product != null) {
                    BigDecimal itemTotal = BigDecimal.valueOf(product.getPrice()).multiply(BigDecimal.valueOf(item.getQuantity()));
                    eligibleTotal = eligibleTotal.add(itemTotal);
                }
            }
        }

        // Check if no eligible products found
        if (eligibleTotal.compareTo(BigDecimal.ZERO) == 0) {
            alerts.add("No products eligible for applied coupon. Discount removed.");
            cart.setAppliedCouponId(null);
            cart.setDiscountAmount(BigDecimal.ZERO);
            cartRepository.save(cart);
            return;
        }

        // Check if eligible total is less than minimum required (CRITICAL BUG FIX)
        if (eligibleTotal.compareTo(BigDecimal.valueOf(coupon.getMinOrderAmount())) < 0) {
            alerts.add("Eligible products total (₹" + eligibleTotal + ") below minimum (₹" + coupon.getMinOrderAmount() + "). Discount removed.");
            cart.setAppliedCouponId(null);
            cart.setDiscountAmount(BigDecimal.ZERO);
            cartRepository.save(cart);
        }
    }

    private EligibleCouponsResponse.CouponOption evaluateCouponForCart(Coupon coupon, Cart cart) {
        // Check 1: Expired?
        if (coupon.getValidTill().isBefore(LocalDateTime.now())) {
            return EligibleCouponsResponse.CouponOption.builder()
                    .couponId(coupon.getId())
                    .code(coupon.getCode())
                    .isEligible(false)
                    .reason("Coupon expired on " + coupon.getValidTill())
                    .build();
        }

        // Check 2: Active?
        if (!coupon.getActive()) {
            return EligibleCouponsResponse.CouponOption.builder()
                    .couponId(coupon.getId())
                    .code(coupon.getCode())
                    .isEligible(false)
                    .reason("Coupon is not active")
                    .build();
        }

        // Check 3: Usage limit
        if (coupon.getUsedCount() >= coupon.getTotalUsageLimit()) {
            return EligibleCouponsResponse.CouponOption.builder()
                    .couponId(coupon.getId())
                    .code(coupon.getCode())
                    .isEligible(false)
                    .reason("Coupon usage limit exceeded")
                    .build();
        }

        // Get eligible products for this coupon
        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());
        BigDecimal eligibleTotal = BigDecimal.ZERO;

        for (CartItem item : items) {
            if (productCouponRepository.findByProductIdAndCouponId(item.getProductId(), coupon.getId()).isPresent()) {
                Product product = productRepository.findById(item.getProductId()).orElse(null);
                if (product != null) {
                    BigDecimal itemTotal = BigDecimal.valueOf(product.getPrice()).multiply(BigDecimal.valueOf(item.getQuantity()));
                    eligibleTotal = eligibleTotal.add(itemTotal);
                }
            }
        }

        // Check 4: Criteria - minimum amount
        if (eligibleTotal.compareTo(BigDecimal.valueOf(coupon.getMinOrderAmount())) < 0) {
            return EligibleCouponsResponse.CouponOption.builder()
                    .couponId(coupon.getId())
                    .code(coupon.getCode())
                    .isEligible(false)
                    .reason("Minimum order ₹" + coupon.getMinOrderAmount() + " required (you have ₹" + eligibleTotal + ")")
                    .build();
        }

        // Calculate discount
        BigDecimal discount = calculateDiscount(eligibleTotal, coupon);

        return EligibleCouponsResponse.CouponOption.builder()
                .couponId(coupon.getId())
                .code(coupon.getCode())
                .description(coupon.getDiscountValue() + "% off" + (coupon.getMinOrderAmount() > 0 ? " (min ₹" + coupon.getMinOrderAmount() + ")" : ""))
                .discountAmount(discount)
                .discountType(coupon.getDiscountType().name())
                .isEligible(true)
                .build();
    }

    private String validateCouponForApply(Coupon coupon, Cart cart) {
        // Check: Expired
        if (coupon.getValidTill().isBefore(LocalDateTime.now())) {
            return "Coupon expired";
        }

        // Check: Active
        if (!coupon.getActive()) {
            return "Coupon is not active";
        }

        // Check: Usage limit
        if (coupon.getUsedCount() >= coupon.getTotalUsageLimit()) {
            return "Coupon usage limit exceeded";
        }

        // Check: Eligible products exist
        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());
        BigDecimal eligibleTotal = BigDecimal.ZERO;

        for (CartItem item : items) {
            if (productCouponRepository.findByProductIdAndCouponId(item.getProductId(), coupon.getId()).isPresent()) {
                Product product = productRepository.findById(item.getProductId()).orElse(null);
                if (product != null) {
                    BigDecimal itemTotal = BigDecimal.valueOf(product.getPrice()).multiply(BigDecimal.valueOf(item.getQuantity()));
                    eligibleTotal = eligibleTotal.add(itemTotal);
                }
            }
        }

        // Check: Minimum amount
        if (eligibleTotal.compareTo(BigDecimal.valueOf(coupon.getMinOrderAmount())) < 0) {
            return "Minimum order ₹" + coupon.getMinOrderAmount() + " required";
        }

        // Calculate and store discount
        BigDecimal discount = calculateDiscount(eligibleTotal, coupon);
        cart.setDiscountAmount(discount);

        return null;
    }

    private boolean isCouponStillValidForCart(Cart cart) {
        if (cart.getAppliedCouponId() == null) {
            return true;
        }

        Coupon coupon = couponRepository.findById(cart.getAppliedCouponId()).orElse(null);
        if (coupon == null || !coupon.getActive() || coupon.getValidTill().isBefore(LocalDateTime.now())) {
            return false;
        }

        // Check if any item is still eligible AND eligible total meets minimum (CRITICAL BUG FIX)
        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());
        BigDecimal eligibleTotal = BigDecimal.ZERO;
        
        for (CartItem item : items) {
            if (productCouponRepository.findByProductIdAndCouponId(item.getProductId(), coupon.getId()).isPresent()) {
                Product product = productRepository.findById(item.getProductId()).orElse(null);
                if (product != null) {
                    BigDecimal itemTotal = BigDecimal.valueOf(product.getPrice()).multiply(BigDecimal.valueOf(item.getQuantity()));
                    eligibleTotal = eligibleTotal.add(itemTotal);
                }
            }
        }

        // Must have eligible products AND meet minimum order amount
        return eligibleTotal.compareTo(BigDecimal.ZERO) > 0 && 
               eligibleTotal.compareTo(BigDecimal.valueOf(coupon.getMinOrderAmount())) >= 0;
    }

    private BigDecimal calculateDiscount(BigDecimal eligibleTotal, Coupon coupon) {
        BigDecimal discount = BigDecimal.ZERO;

        if (coupon.getDiscountType().name().equals("PERCENTAGE")) {
            discount = eligibleTotal.multiply(BigDecimal.valueOf(coupon.getDiscountValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
        } else {
            // FLAT
            discount = BigDecimal.valueOf(coupon.getDiscountValue());
            discount = discount.min(eligibleTotal);  // Can't discount more than eligible total
        }

        return discount;
    }
}
