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
import com.snackecommerce.product.repository.ProductRepository;
import com.snackecommerce.product.service.CouponService;
import com.snackecommerce.order.entity.Order;
import com.snackecommerce.order.entity.OrderItem;
import com.snackecommerce.order.enums.OrderStatus;
import com.snackecommerce.order.repository.OrderRepository;
import com.snackecommerce.order.repository.OrderItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class CartService {

    private static final Logger logger = LoggerFactory.getLogger(CartService.class);

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CouponService couponService;

    // ==================== GET OR CREATE CART ====================

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

    // ==================== ADD TO CART ====================

    public CartResponse addToCart(Long userId, AddToCartRequest request) {
        if (request.getQuantity() < 1 || request.getQuantity() > 100) {
            throw new InvalidCartItemException("Quantity must be between 1 and 100");
        }

        Cart cart = getOrCreateCart(userId);

        // Check product exists and is available
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + request.getProductId()));

        if (!product.getIsAvailable()) {
            throw new InvalidCartItemException("Product is not available: " + product.getName());
        }

        BigDecimal currentPrice = product.getPrice();

        // Check if product already in cart
        Optional<CartItem> existingItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), request.getProductId());
        
        CartItem cartItem;
        List<String> alerts = new ArrayList<>();

        if (existingItem.isPresent()) {
            cartItem = existingItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());
            
            // Check price changes
            if (!cartItem.getSnapshotPrice().equals(currentPrice)) {
                BigDecimal priceDiff = currentPrice.subtract(cartItem.getSnapshotPrice());
                String direction = priceDiff.compareTo(BigDecimal.ZERO) > 0 ? "↑" : "↓";
                alerts.add("Price " + direction + " ₹" + cartItem.getSnapshotPrice() + " → ₹" + currentPrice);
                cartItem.setSnapshotPrice(currentPrice);
            }
        } else {
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

        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        // Validate coupon eligibility after adding item
        validateAndRemoveCouponIfNeeded(cart, alerts);

        return getCartResponse(cart, alerts);
    }

    // ==================== REMOVE FROM CART ====================

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

        // Validate coupon eligibility after removing item
        validateAndRemoveCouponIfNeeded(cart, alerts);

        return getCartResponse(cart, alerts);
    }

    // ==================== UPDATE QUANTITY ====================

    public CartResponse updateQuantity(Long userId, Long cartItemId, Integer newQuantity) {
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

        BigDecimal currentPrice = product.getPrice();
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

        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        // Validate coupon eligibility after updating quantity
        validateAndRemoveCouponIfNeeded(cart, alerts);

        return getCartResponse(cart, alerts);
    }

    // ==================== VIEW CART ====================

    public CartResponse getCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found for user: " + userId));

        List<String> alerts = new ArrayList<>();
        
        // Validate all products are available
        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());
        for (CartItem item : items) {
            Product product = productRepository.findById(item.getProductId()).orElse(null);
            if (product == null || !product.getIsAvailable()) {
                cartItemRepository.delete(item);
                alerts.add("Product no longer available. Removed from cart");
            }
        }

        return getCartResponse(cart, alerts);
    }

    // ==================== GET ELIGIBLE COUPONS ====================

    public EligibleCouponsResponse getEligibleCoupons(Long userId) {
        // Step 1: Fetch cart
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found for user: " + userId));

        // Step 2: Fetch cart items
        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
        
        if (cartItems.isEmpty()) {
            return EligibleCouponsResponse.builder()
                    .eligibleCoupons(new ArrayList<>())
                    .ineligibleCoupons(new ArrayList<>())
                    .build();
        }

        // Step 3: Fetch all products once
        List<Product> allProducts = productRepository.findAll();

        // Step 4: Calculate subtotal - ONLY for coupon-eligible items
        BigDecimal eligibleSubtotal = BigDecimal.ZERO;
        boolean hasCouponEligibleProducts = false;

        for (CartItem item : cartItems) {
            // Find product for this item
            Product product = null;
            for (Product p : allProducts) {
                if (p.getId().equals(item.getProductId())) {
                    product = p;
                    break;
                }
            }

            // If product is coupon-eligible, add to subtotal
            if (product != null && product.getIsEligibleForCoupon()) {
                hasCouponEligibleProducts = true;
                BigDecimal itemAmount = item.getSnapshotPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                eligibleSubtotal = eligibleSubtotal.add(itemAmount);
            }
        }

        List<EligibleCouponsResponse.CouponOption> eligible = new ArrayList<>();
        List<EligibleCouponsResponse.CouponOption> ineligible = new ArrayList<>();

        List<Coupon> allCoupons = couponRepository.findAll();

        // Step 5: For each coupon, check eligibility
        for (Coupon coupon : allCoupons) {
            String ineligibilityReason = null;

            // Check 1: Is coupon active and not expired?
            if (!coupon.getActive() || coupon.getValidTill().isBefore(LocalDateTime.now())) {
                ineligibilityReason = "Coupon is not active or expired";
            }

            // Check 2: Does cart have coupon-eligible products?
            if (ineligibilityReason == null && !hasCouponEligibleProducts) {
                ineligibilityReason = "No coupon-eligible products in cart";
            }

            // Check 3: Does eligible subtotal meet minimum amount required?
            if (ineligibilityReason == null && coupon.getMinOrderAmount() != null) {
                if (eligibleSubtotal.compareTo(coupon.getMinOrderAmount()) < 0) {
                    ineligibilityReason = "Minimum order amount ₹" + coupon.getMinOrderAmount() + " required";
                }
            }

            if (ineligibilityReason != null) {
                ineligible.add(EligibleCouponsResponse.CouponOption.builder()
                        .id(coupon.getId())
                        .code(coupon.getCode())
                        .description(coupon.getCode() + " - " + coupon.getType() + " coupon")
                        .discountValue(coupon.getDiscountValue())
                        .type(coupon.getType().toString())
                        .minOrderAmount(coupon.getMinOrderAmount())
                        .active(coupon.getActive())
                        .validFrom(coupon.getValidFrom())
                        .validTill(coupon.getValidTill())
                        .isEligible(false)
                        .reason(ineligibilityReason)
                        .build());
            } else {
                // Calculate actual discount based on coupon type and eligible subtotal
                BigDecimal discountForDisplay;
                if ("FLAT".equals(coupon.getType().toString())) {
                    discountForDisplay = coupon.getDiscountValue();
                } else {
                    // PERCENTAGE type
                    discountForDisplay = eligibleSubtotal.multiply(coupon.getDiscountValue()).divide(BigDecimal.valueOf(100));
                }
                
                eligible.add(EligibleCouponsResponse.CouponOption.builder()
                        .id(coupon.getId())
                        .code(coupon.getCode())
                        .description(coupon.getCode() + " - Save ₹" + discountForDisplay)
                        .discountValue(coupon.getDiscountValue())
                        .type(coupon.getType().toString())
                        .minOrderAmount(coupon.getMinOrderAmount())
                        .active(coupon.getActive())
                        .validFrom(coupon.getValidFrom())
                        .validTill(coupon.getValidTill())
                        .isEligible(true)
                        .build());
            }
        }

        return EligibleCouponsResponse.builder()
                .eligibleCoupons(eligible)
                .ineligibleCoupons(ineligible)
                .build();
    }

    // ==================== APPLY COUPON ====================

    public CartResponse applyCoupon(Long userId, ApplyCouponRequest request) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found for user: " + userId));

        Coupon coupon = couponRepository.findById(request.getCouponId())
                .orElseThrow(() -> new CouponNotFoundException("Coupon not found: " + request.getCouponId()));

        if (!coupon.getActive() || coupon.getValidTill().isBefore(LocalDateTime.now())) {
            throw new InvalidCouponStateException("Coupon is not active or has expired");
        }

        // Calculate subtotal and validate minimum order amount
        BigDecimal subtotal = calculateSubtotal(cart);
        couponService.validateCouponEligibility(coupon, subtotal);

        // Calculate discount based on type (FLAT or PERCENTAGE)
        BigDecimal discountAmount = couponService.calculateDiscount(coupon, subtotal);

        cart.setAppliedCouponId(coupon.getId());
        cart.setDiscountAmount(discountAmount);
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        List<String> alerts = new ArrayList<>();
        String discountDesc = coupon.getType().toString().equals("FLAT")
            ? "₹" + discountAmount
            : String.valueOf(discountAmount);
        alerts.add("Coupon applied: " + coupon.getCode() + " (" + discountDesc + " off)");

        return getCartResponse(cart, alerts);
    }

    // ==================== REMOVE COUPON ====================

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

    // ==================== CHECKOUT VALIDATION ====================

    public CheckoutValidationResponse validateCheckout(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found for user: " + userId));

        List<String> issues = new ArrayList<>();
        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());

        if (items.isEmpty()) {
            return CheckoutValidationResponse.builder()
                    .isValid(false)
                    .message("Cart is empty")
                    .issues(List.of("Add products to cart before checkout"))
                    .build();
        }

        // Validate all items exist and are available
        List<CartItemResponse> validatedItems = new ArrayList<>();

        for (CartItem item : items) {
            Product product = productRepository.findById(item.getProductId()).orElse(null);

            if (product == null) {
                issues.add("Product not found. Removed from cart");
                cartItemRepository.delete(item);
                continue;
            }
            if (!product.getIsAvailable()) {
                issues.add("Product " + product.getName() + " is no longer available");
                cartItemRepository.delete(item);
                continue;
            }

            BigDecimal currentPrice = product.getPrice();
            BigDecimal itemTotal = currentPrice.multiply(BigDecimal.valueOf(item.getQuantity()));

            validatedItems.add(CartItemResponse.builder()
                    .cartItemId(item.getId())
                    .productId(item.getProductId())
                    .productName(item.getProductNameSnapshot())
                    .quantity(item.getQuantity())
                    .snapshotPrice(item.getSnapshotPrice())
                    .currentPrice(currentPrice)
                    .itemTotal(itemTotal)
                    .build());
        }

        if (validatedItems.isEmpty()) {
            return CheckoutValidationResponse.builder()
                    .isValid(false)
                    .message("All items became invalid")
                    .issues(issues)
                    .build();
        }

        // Validate coupon if applied
        if (cart.getAppliedCouponId() != null) {
            Coupon coupon = couponRepository.findById(cart.getAppliedCouponId()).orElse(null);
            if (coupon == null || !coupon.getActive() || coupon.getValidTill().isBefore(LocalDateTime.now())) {
                issues.add("Applied coupon is no longer valid");
                cart.setAppliedCouponId(null);
                cart.setDiscountAmount(BigDecimal.ZERO);
                cartRepository.save(cart);
            }
        }

        BigDecimal subtotal = validatedItems.stream()
                .map(CartItemResponse::getItemTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Validate minimum order amount for applied coupon
        if (cart.getAppliedCouponId() != null) {
            Coupon coupon = couponRepository.findById(cart.getAppliedCouponId()).orElse(null);
            if (coupon != null && coupon.getMinOrderAmount() != null && subtotal.compareTo(coupon.getMinOrderAmount()) < 0) {
                issues.add("Cart amount (₹" + subtotal + ") is below minimum required (₹" + coupon.getMinOrderAmount() + ") for coupon: " + coupon.getCode());
                cart.setAppliedCouponId(null);
                cart.setDiscountAmount(BigDecimal.ZERO);
                cartRepository.save(cart);
            }
        }

        BigDecimal discount = cart.getDiscountAmount() != null ? cart.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal finalTotal = subtotal.subtract(discount);

        if (!issues.isEmpty()) {
            return CheckoutValidationResponse.builder()
                    .isValid(false)
                    .message("Checkout validation failed. Please review your cart.")
                    .issues(issues)
                    .build();
        }

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

    // ==================== CHECKOUT ====================

    /**
     * Proceed to checkout and create order with address reference
     * @param userId User ID
     * @param addressId Address ID for delivery
     * @param receiverName Receiver name
     * @param receiverPhone Receiver phone
     * @param receiverEmail Receiver email
     * @return Created order
     */
    public Order proceedToCheckout(Long userId, Long addressId, String receiverName, 
                                   String receiverPhone, String receiverEmail) throws Exception {
        Cart cart = cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .orElseThrow(() -> new CartNotFoundException("No active cart found for user: " + userId));

        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
        if (cartItems.isEmpty()) {
            throw new CartNotFoundException("Cart is empty");
        }

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (CartItem cartItem : cartItems) {
            Product product = productRepository.findById(cartItem.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException("Product not found: " + cartItem.getProductId()));

            OrderItem orderItem = OrderItem.builder()
                    .productId(product.getId())
                    .productNameSnapshot(product.getName())
                    .unitPriceSnapshot(product.getPrice())
                    .quantity(cartItem.getQuantity())
                    .build();
            orderItems.add(orderItem);

            subtotal = subtotal.add(product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }

        BigDecimal discountAmount = cart.getDiscountAmount() != null ? cart.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal totalAmount = subtotal.subtract(discountAmount);

        // Create order with address reference for delivery
        Order order = Order.builder()
                .orderNumber("ORD-" + System.currentTimeMillis() + "-" + userId)
                .userId(userId)
                .status(OrderStatus.PAYMENT_PENDING)
                .subtotal(subtotal)
                .discountAmount(discountAmount)
                .totalAmountBigDecimal(totalAmount)
                .totalAmount(totalAmount.doubleValue())
                .appliedCouponId(cart.getAppliedCouponId())
                .cartId(cart.getId())
                // Store address reference
                .addressId(addressId)
                .receiverName(receiverName)
                .receiverPhone(receiverPhone)
                .receiverEmail(receiverEmail)
                .build();

        order = orderRepository.save(order);

        for (OrderItem orderItem : orderItems) {
            orderItem.setOrderId(order.getId());
            orderItemRepository.save(orderItem);
        }

        logger.info("Order created with ID: {} for user: {} with addressId: {}", 
                   order.getId(), userId, addressId);
        
        return order;
    }

    // ==================== HELPER METHODS ====================

    private CartResponse getCartResponse(Cart cart, List<String> alerts) {
        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());
        List<CartItemResponse> itemResponses = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (CartItem item : items) {
            Product product = productRepository.findById(item.getProductId()).orElse(null);

            if (product == null || !product.getIsAvailable()) {
                cartItemRepository.delete(item);
                alerts.add("Product no longer available. Removed from cart");
                continue;
            }

            BigDecimal currentPrice = product.getPrice();
            BigDecimal itemTotal = currentPrice.multiply(BigDecimal.valueOf(item.getQuantity()));

            itemResponses.add(CartItemResponse.builder()
                    .cartItemId(item.getId())
                    .productId(item.getProductId())
                    .productName(item.getProductNameSnapshot())
                    .quantity(item.getQuantity())
                    .snapshotPrice(item.getSnapshotPrice())
                    .currentPrice(currentPrice)
                    .itemTotal(itemTotal)
                    .build());

            subtotal = subtotal.add(itemTotal);
        }

        // Calculate FRESH discount on-the-fly based on current cart subtotal
        // This ensures discount is always accurate even if quantities changed
        BigDecimal discount = calculateFreshDiscount(cart, alerts);
        BigDecimal total = subtotal.subtract(discount);

        // Get applied coupon code for response
        String appliedCouponCode = null;
        if (cart.getAppliedCouponId() != null) {
            try {
                Coupon coupon = couponRepository.findById(cart.getAppliedCouponId()).orElse(null);
                if (coupon != null) {
                    appliedCouponCode = coupon.getCode();
                }
            } catch (Exception e) {
                logger.warn("Error fetching applied coupon code", e);
            }
        }

        return CartResponse.builder()
                .cartId(cart.getId())
                .userId(cart.getUserId())
                .items(itemResponses)
                .subtotal(subtotal)
                .discountAmount(discount)
                .total(total)
                .appliedCouponId(cart.getAppliedCouponId())
                .appliedCouponCode(appliedCouponCode)
                .alerts(alerts)
                .build();
    }

    // ==================== HELPER METHODS ====================

    private BigDecimal calculateSubtotal(Cart cart) {
        BigDecimal subtotal = BigDecimal.ZERO;
        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
        
        for (CartItem item : cartItems) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException("Product not found: " + item.getProductId()));
            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            subtotal = subtotal.add(itemTotal);
        }
        
        return subtotal;
    }

    /**
     * Calculate fresh discount amount on-the-fly based on current cart subtotal.
     * Validates coupon eligibility and removes it if no longer valid.
     * Returns zero if coupon is no longer eligible.
     */
    private BigDecimal calculateFreshDiscount(Cart cart, List<String> alerts) {
        if (cart.getAppliedCouponId() == null) {
            return BigDecimal.ZERO; // No coupon applied
        }

        try {
            Coupon coupon = couponRepository.findById(cart.getAppliedCouponId())
                    .orElse(null);

            if (coupon == null) {
                // Coupon was deleted, remove it from cart
                cart.setAppliedCouponId(null);
                cartRepository.save(cart);
                alerts.add("Applied coupon no longer available. Removed from cart");
                return BigDecimal.ZERO;
            }

            // Check if coupon is still active and not expired
            if (!coupon.getActive() || coupon.getValidTill().isBefore(LocalDateTime.now())) {
                cart.setAppliedCouponId(null);
                cartRepository.save(cart);
                alerts.add("Applied coupon is no longer active or has expired. Removed from cart");
                return BigDecimal.ZERO;
            }

            // Calculate current subtotal
            BigDecimal currentSubtotal = calculateSubtotal(cart);
            
            // Check minimum order amount eligibility
            if (coupon.getMinOrderAmount() != null && currentSubtotal.compareTo(coupon.getMinOrderAmount()) < 0) {
                cart.setAppliedCouponId(null);
                cartRepository.save(cart);
                alerts.add("Cart amount (₹" + currentSubtotal + ") below minimum (₹" + coupon.getMinOrderAmount() + ") for coupon. Removed");
                return BigDecimal.ZERO;
            }

            // Calculate fresh discount based on CURRENT subtotal
            BigDecimal freshDiscount = couponService.calculateDiscount(coupon, currentSubtotal);
            return freshDiscount;
            
        } catch (Exception e) {
            // Log error but don't fail the operation
            logger.warn("Error calculating fresh coupon discount", e);
            return BigDecimal.ZERO;
        }
    }

    /**
     * Validate if applied coupon is still eligible based on current cart subtotal.
     * If not eligible, remove the coupon and add alert message.
     */
    private void validateAndRemoveCouponIfNeeded(Cart cart, List<String> alerts) {
        if (cart.getAppliedCouponId() == null) {
            return; // No coupon applied
        }

        try {
            Coupon coupon = couponRepository.findById(cart.getAppliedCouponId())
                    .orElse(null);

            if (coupon == null) {
                // Coupon was deleted, remove it from cart
                cart.setAppliedCouponId(null);
                cart.setDiscountAmount(BigDecimal.ZERO);
                cartRepository.save(cart);
                alerts.add("Applied coupon no longer available. Removed from cart");
                return;
            }

            // Check if coupon is still active and not expired
            if (!coupon.getActive() || coupon.getValidTill().isBefore(LocalDateTime.now())) {
                cart.setAppliedCouponId(null);
                cart.setDiscountAmount(BigDecimal.ZERO);
                cartRepository.save(cart);
                alerts.add("Applied coupon is no longer active or has expired. Removed from cart");
                return;
            }

            // Check minimum order amount eligibility
            BigDecimal currentSubtotal = calculateSubtotal(cart);
            if (coupon.getMinOrderAmount() != null && currentSubtotal.compareTo(coupon.getMinOrderAmount()) < 0) {
                cart.setAppliedCouponId(null);
                cart.setDiscountAmount(BigDecimal.ZERO);
                cartRepository.save(cart);
                alerts.add("Cart amount below minimum (₹" + coupon.getMinOrderAmount() + ") for coupon. Coupon removed");
            }
        } catch (Exception e) {
            // Log error but don't fail the operation
            org.slf4j.LoggerFactory.getLogger(CartService.class)
                    .warn("Error validating coupon eligibility", e);
        }
    }
}

