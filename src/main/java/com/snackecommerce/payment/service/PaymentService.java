package com.snackecommerce.payment.service;

import com.snackecommerce.cart.entity.Cart;
import com.snackecommerce.cart.entity.CartItem;
import com.snackecommerce.cart.repository.CartItemRepository;
import com.snackecommerce.cart.repository.CartRepository;
import com.snackecommerce.common.exception.CartNotFoundException;
import com.snackecommerce.common.exception.CouponLimitExceededException;
import com.snackecommerce.order.entity.Order;
import com.snackecommerce.order.entity.OrderItem;
import com.snackecommerce.order.enums.OrderStatus;
import com.snackecommerce.order.repository.OrderItemRepository;
import com.snackecommerce.order.repository.OrderRepository;
import com.snackecommerce.payment.dto.CreatePaymentRequest;
import com.snackecommerce.payment.dto.PaymentResponse;
import com.snackecommerce.payment.entity.Payment;
import com.snackecommerce.payment.enums.PaymentProvider;
import com.snackecommerce.payment.enums.PaymentStatus;
import com.snackecommerce.payment.repository.PaymentRepository;
import com.snackecommerce.payment.util.RazorpayUtil;
import com.snackecommerce.product.entity.Coupon;
import com.snackecommerce.product.entity.Product;
import com.snackecommerce.product.repository.CouponRepository;
import com.snackecommerce.product.repository.ProductRepository;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Retryable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Payment Service for Razorpay integration
 * 
 * Production Level Features:
 * - Stock reservation and deduction
 * - Optimistic locking for concurrency
 * - Webhook signature verification
 * - Idempotency for duplicate webhook handling
 * - Comprehensive error handling and logging
 * - Coupon usedCount tracking with retry
 */
@Service
@Transactional
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private RazorpayUtil razorpayUtil;

    @Autowired
    private PaymentCompensationService compensationService;

    /**
     * Create payment order for checkout
     * 
     * Flow:
     * 1. Validate order exists and is in PAYMENT_PENDING status
     * 2. Create Razorpay order via API
     * 3. Reserve stock for all order items
     * 4. Create payment record with idempotency key
     * 5. Return Razorpay order details to frontend
     * 
     * @param request Payment creation request
     * @return Payment response with Razorpay order details
     * @throws Exception if any step fails
     */
    public PaymentResponse createPayment(CreatePaymentRequest request) throws Exception {
        // Fetch the order
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new CartNotFoundException("Order not found with ID: " + request.getOrderId()));

        // Validate order status
        if (!order.getStatus().equals(OrderStatus.PAYMENT_PENDING)) {
            throw new RuntimeException("Invalid order status for payment: " + order.getStatus() + ". Expected: PAYMENT_PENDING");
        }

        logger.info("Creating payment for order ID: {}, amount: {} INR", request.getOrderId(), request.getAmount());

        try {
            // Create Razorpay order
            JSONObject razorpayOrder = razorpayUtil.createOrder(
                request.getAmount(),
                order.getId().toString(),
                request.getEmail(),
                request.getPhone()
            );

            String razorpayOrderId = razorpayOrder.getString("id");

            // Reserve stock for all items in this order
            reserveStock(order.getId());

            // Create payment record
            Payment payment = Payment.builder()
                    .orderId(order.getId())
                    .amount(request.getAmount().doubleValue())
                    .provider(PaymentProvider.RAZORPAY)
                    .status(PaymentStatus.INITIATED)
                    .providerOrderId(razorpayOrderId)
                    .idempotencyKey(order.getId().toString())  // Use order ID as idempotency key
                    .build();

            paymentRepository.save(payment);
            logger.info("Payment record created for Razorpay order: {}", razorpayOrderId);

            // Return response for frontend
            return PaymentResponse.builder()
                    .razorpayOrderId(razorpayOrderId)
                    .amount(request.getAmount())
                    .email(request.getEmail())
                    .phone(request.getPhone())
                    .orderId(order.getId().toString())
                    .build();
        } catch (Exception e) {
            logger.error("Failed to create payment for order ID: {}", request.getOrderId(), e);
            throw e;
        }
    }

    /**
     * Handle successful payment webhook
     * 
     * Flow:
     * 1. Verify webhook signature for security
     * 2. Check idempotency (prevent double-processing)
     * 3. Update payment status to SUCCESS
     * 4. Update order status to CONFIRMED
     * 5. Confirm stock deduction (already reserved)
     * 6. Increment coupon usedCount (with retry for concurrency)
     * 7. Clear cart items
     * 
     * @param razorpayOrderId Razorpay Order ID
     * @param razorpayPaymentId Razorpay Payment ID
     * @param signature Webhook signature for verification
     * @throws Exception if any step fails
     */
    public void handlePaymentSuccess(String razorpayOrderId, String razorpayPaymentId, String signature) throws Exception {
        logger.info("Processing payment success webhook for Razorpay order: {}", razorpayOrderId);

        // Verify webhook signature for security
        if (!razorpayUtil.verifyWebhookSignature(razorpayOrderId, razorpayPaymentId, signature)) {
            logger.error("Webhook signature verification failed for order: {}", razorpayOrderId);
            throw new RuntimeException("Invalid webhook signature - possible tampering detected");
        }

        // Find payment by provider order ID
        Optional<Payment> paymentOptional = paymentRepository.findByProviderOrderId(razorpayOrderId);
        if (paymentOptional.isEmpty()) {
            logger.warn("Payment not found for Razorpay order ID: {}", razorpayOrderId);
            throw new RuntimeException("Payment not found for order ID: " + razorpayOrderId);
        }

        Payment payment = paymentOptional.get();

        // Check if already processed (idempotency)
        if (payment.getStatus().equals(PaymentStatus.SUCCESS)) {
            logger.info("Payment already processed (idempotency check) for order: {}", razorpayOrderId);
            return;
        }

        Order order = orderRepository.findById(payment.getOrderId())
                .orElseThrow(() -> new CartNotFoundException("Order not found"));

        try {
            // Update payment record
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setProviderPaymentId(razorpayPaymentId);
            payment.setConfirmedAt(LocalDateTime.now());
            paymentRepository.save(payment);
            logger.info("Payment status updated to SUCCESS for order: {}", payment.getOrderId());

            // Update order status
            order.setStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);
            logger.info("Order status updated to CONFIRMED for order ID: {}", order.getId());

            // Deduct stock (actually consume the reservation)
            // Wrapped in try-catch to handle stock conflicts from concurrent orders
            try {
                deductStock(order.getId());
                logger.info("Stock deducted for order ID: {}", order.getId());
            } catch (ObjectOptimisticLockingFailureException e) {
                // Stock conflict: Another order deducted same item concurrently
                logger.error("Stock conflict detected for order ID: {}. Item sold out during checkout. Initiating refund...", 
                        order.getId(), e);
                
                // Handle refund in separate transaction (REQUIRES_NEW)
                // Track whether compensation succeeded
                boolean compensationSucceeded = false;
                try {
                    compensationService.handleStockConflictRefund(order, payment);
                    compensationSucceeded = true;
                } catch (RuntimeException compensationError) {
                    logger.error("CRITICAL: Stock conflict compensation failed for order: {} - Scheduler will handle", order.getId(), compensationError);
                    // DON'T rethrow - let scheduler handle it later
                }
                
                if (compensationSucceeded) {
                    // Order is CANCELLED, payment is FAILED, refund issued
                    logger.info("Stock conflict handled - Order cancelled and payment refunded: {}", order.getId());
                    throw new RuntimeException("Item sold out. Payment refunded. Order cancelled.", e);
                } else {
                    // Compensation failed and rolled back - order still PAYMENT_PENDING
                    // Scheduler will detect and handle it
                    logger.warn("Stock conflict but compensation failed - Order remains PAYMENT_PENDING, scheduler will fix: {}", order.getId());
                    throw new RuntimeException("Item sold out. Compensation failed - will be handled by scheduler.", e);
                }
            }

            // Validate and increment coupon if applied
            // If validation fails, refund payment and cancel order
            if (order.getAppliedCouponId() != null) {
                try {
                    validateAndIncrementCoupon(order.getAppliedCouponId());
                } catch (RuntimeException e) {
                    logger.error("Coupon validation failed after payment success. Initiating refund for order: {}", order.getId(), e);
                    
                    // Handle refund in separate transaction (REQUIRES_NEW)
                    // Track whether compensation succeeded
                    boolean compensationSucceeded = false;
                    try {
                        compensationService.handleCouponValidationRefund(order, payment, e);
                        compensationSucceeded = true;
                    } catch (RuntimeException compensationError) {
                        logger.error("CRITICAL: Coupon validation compensation failed for order: {} - Scheduler will handle", order.getId(), compensationError);
                        // DON'T rethrow - let scheduler handle it later
                    }
                    
                    if (compensationSucceeded) {
                        // Order is CANCELLED, payment is FAILED, refund issued
                        logger.info("Coupon validation handled - Order cancelled and payment refunded: {}", order.getId());
                        throw new RuntimeException("Coupon validation failed. Payment refunded. Order cancelled.", e);
                    } else {
                        // Compensation failed and rolled back - order still PAYMENT_PENDING
                        // Scheduler will detect and handle it
                        logger.warn("Coupon validation failed but compensation failed - Order remains PAYMENT_PENDING, scheduler will fix: {}", order.getId());
                        throw new RuntimeException("Coupon validation failed. Compensation failed - will be handled by scheduler.", e);
                    }
                }
            }

            // Clear cart items for this user
            clearUserCart(order.getUserId());
            logger.info("Cart cleared for user ID: {}", order.getUserId());

        } catch (ObjectOptimisticLockingFailureException e) {
            logger.error("Optimistic locking failure during payment success processing", e);
            throw new RuntimeException("Order was modified concurrently. Please retry.", e);
        } catch (RuntimeException e) {
            logger.error("Unexpected runtime exception during payment success processing for order. Rolling back all changes.", e);
            throw e;
        }
    }

    /**
     * Handle failed payment webhook
     * 
     * Flow:
     * 1. Verify webhook signature (if available)
     * 2. Update payment status to FAILED
     * 3. Update order status to CANCELLED
     * 4. Release stock reservation
     * 5. Keep cart items (user can retry)
     * 
     * @param razorpayOrderId Razorpay Order ID
     * @param razorpayPaymentId Razorpay Payment ID
     * @throws Exception if any step fails
     */
    public void handlePaymentFailure(String razorpayOrderId, String razorpayPaymentId) throws Exception {
        logger.info("Processing payment failure webhook for Razorpay order: {}", razorpayOrderId);

        // Find payment by provider order ID
        Optional<Payment> paymentOptional = paymentRepository.findByProviderOrderId(razorpayOrderId);
        if (paymentOptional.isEmpty()) {
            logger.warn("Payment not found for failed Razorpay order ID: {}", razorpayOrderId);
            throw new RuntimeException("Payment not found for order ID: " + razorpayOrderId);
        }

        Payment payment = paymentOptional.get();

        // Check if already processed
        if (payment.getStatus().equals(PaymentStatus.FAILED)) {
            logger.info("Payment failure already processed for order: {}", razorpayOrderId);
            return;
        }

        Order order = orderRepository.findById(payment.getOrderId())
                .orElseThrow(() -> new CartNotFoundException("Order not found"));

        try {
            // Update payment record
            payment.setStatus(PaymentStatus.FAILED);
            payment.setProviderPaymentId(razorpayPaymentId);
            paymentRepository.save(payment);
            logger.info("Payment status updated to FAILED for order: {}", payment.getOrderId());

            // Update order status
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
            logger.info("Order status updated to CANCELLED for order ID: {}", order.getId());

            // Release stock reservation
            releaseStock(order.getId());
            logger.info("Stock reservation released for order ID: {}", order.getId());

            // Note: Cart items are kept so user can retry payment
        } catch (ObjectOptimisticLockingFailureException e) {
            logger.error("Optimistic locking failure during payment failure processing", e);
            throw new RuntimeException("Order was modified concurrently. Please retry.", e);
        }
    }

    /**
     * Reserve stock for an order
     * Called when payment is initiated
     * Decrements both stockQuantity and increments reservedQuantity
     * 
     * @param orderId Order ID
     * @throws Exception if stock insufficient or other errors
     */
    private void reserveStock(Long orderId) throws Exception {
        // Get all items for this order
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);

        if (orderItems.isEmpty()) {
            logger.warn("No order items found for order ID: {}", orderId);
            return;
        }

        for (OrderItem orderItem : orderItems) {
            Product product = productRepository.findById(orderItem.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found with ID: " + orderItem.getProductId()));

            // Check if enough stock available (considering already reserved quantities)
            int availableStock = product.getStockQuantity() - product.getReservedQuantity();
            if (availableStock < orderItem.getQuantity()) {
                logger.error("Insufficient stock for product ID: {}. Available: {}, Requested: {}",
                        orderItem.getProductId(), availableStock, orderItem.getQuantity());
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }

            // Reserve stock
            product.setReservedQuantity(product.getReservedQuantity() + orderItem.getQuantity());
            productRepository.save(product);
            logger.info("Stock reserved for product ID: {}, quantity: {}", 
                    orderItem.getProductId(), orderItem.getQuantity());
        }
    }

    /**
     * Release stock reservation after payment failure or timeout
     * Restores reservation to available pool
     * Public method used by PaymentReconciliationScheduler
     * 
     * @param orderId Order ID
     */
    public void releaseStockReservation(Long orderId) {
        releaseStock(orderId);
    }

    /**
     * Release stock reservation (private helper)
     * Restores reservation to available pool
     * 
     * @param orderId Order ID
     */
    private void releaseStock(Long orderId) {
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);

        for (OrderItem orderItem : orderItems) {
            Product product = productRepository.findById(orderItem.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found with ID: " + orderItem.getProductId()));

            // Release reservation
            product.setReservedQuantity(Math.max(0, product.getReservedQuantity() - orderItem.getQuantity()));
            productRepository.save(product);
            logger.info("Stock reservation released for product ID: {}, quantity: {}",
                    orderItem.getProductId(), orderItem.getQuantity());
        }
    }

    /**
     * Deduct stock after payment success
     * Converts reservation into actual deduction
     * 
     * @param orderId Order ID
     */
    private void deductStock(Long orderId) {
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);

        for (OrderItem orderItem : orderItems) {
            Product product = productRepository.findById(orderItem.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found with ID: " + orderItem.getProductId()));

            // Deduct stock and release reservation
            product.setStockQuantity(Math.max(0, product.getStockQuantity() - orderItem.getQuantity()));
            product.setReservedQuantity(Math.max(0, product.getReservedQuantity() - orderItem.getQuantity()));
            productRepository.save(product);
            logger.info("Stock deducted for product ID: {}, quantity: {}", 
                    orderItem.getProductId(), orderItem.getQuantity());
        }
    }

    /**
     * Validate coupon before incrementing usedCount
     * Double-check after payment succeeded (catches race conditions)
     * 
     * Validations:
     * 1. Is coupon active?
     * 2. Has coupon expired?
     * 3. Is total usage limit exceeded?
     * 
     * If any check fails, throws exception and payment is refunded
     * 
     * @param couponId Coupon ID
     * @throws CouponLimitExceededException if limit reached
     * @throws RuntimeException if coupon inactive or expired
     */
    private void validateAndIncrementCoupon(Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new RuntimeException("Coupon not found with ID: " + couponId));

        // ✅ Check 1: Is coupon active?
        if (!coupon.getActive()) {
            throw new RuntimeException("Coupon is inactive. Cannot apply.");
        }

        // ✅ Check 2: Has coupon expired?
        if (coupon.getValidTill() != null && LocalDateTime.now().isAfter(coupon.getValidTill())) {
            throw new RuntimeException("Coupon has expired on " + coupon.getValidTill());
        }

        // ✅ Check 3: Is total usage limit exceeded?
        if (coupon.getTotalUsageLimit() != null && coupon.getUsedCount() >= coupon.getTotalUsageLimit()) {
            logger.warn("Coupon limit exceeded for coupon ID: {}. Current: {}/{}", 
                    couponId, coupon.getUsedCount(), coupon.getTotalUsageLimit());
            throw new CouponLimitExceededException(
                "Coupon usage limit exceeded. Current usage: " + coupon.getUsedCount() + 
                "/" + coupon.getTotalUsageLimit()
            );
        }

        // All validations passed - now increment with retry for concurrency
        incrementCouponUsedCount(couponId);
    }

    /**
     * Increment coupon usedCount with retry for optimistic locking
     * Uses @Retryable to handle concurrent updates
     * NOTE: Caller MUST validate coupon first via validateAndIncrementCoupon()
     * 
     * @param couponId Coupon ID
     */
    @Retryable(
        retryFor = ObjectOptimisticLockingFailureException.class,
        maxAttempts = 3,
        backoff = @org.springframework.retry.annotation.Backoff(delay = 100)
    )
    private void incrementCouponUsedCount(Long couponId) {
        try {
            Coupon coupon = couponRepository.findById(couponId)
                    .orElseThrow(() -> new RuntimeException("Coupon not found with ID: " + couponId));

            // Increment used count (validation already done in validateAndIncrementCoupon)
            coupon.setUsedCount(coupon.getUsedCount() + 1);
            couponRepository.save(coupon);
            logger.info("Coupon usedCount incremented for coupon ID: {}, new count: {}", 
                    couponId, coupon.getUsedCount());
        } catch (ObjectOptimisticLockingFailureException e) {
            logger.warn("Optimistic lock failure while incrementing coupon count. Retrying... for coupon ID: {}", couponId);
            throw e;  // Will trigger retry
        }
    }

    /**
     * Refund payment to customer
     * Called when order processing fails after successful payment
     * Examples: Coupon validation failed, stock issues, etc.
     * 
     * @param payment Payment entity with Razorpay payment ID
     * @param reason Reason for refund (logged and sent to Razorpay)
     */
    private void refundPayment(Payment payment, String reason) {
        try {
            logger.info("Initiating refund for payment ID: {}, reason: {}", 
                    payment.getProviderPaymentId(), reason);
            
            // Call Razorpay API to refund
            razorpayUtil.refundPayment(payment.getProviderPaymentId(), reason);
            
            logger.info("Refund successfully initiated for payment ID: {}", 
                    payment.getProviderPaymentId());
        } catch (Exception e) {
            logger.error("CRITICAL: Failed to initiate refund for payment ID: {}. Manual intervention needed!", 
                    payment.getProviderPaymentId(), e);
            // Even if refund fails, we mark order as cancelled
            // Manual verification required
        }
    }

    /**
     * Clear cart items for a user
     * Called after successful payment
     * 
     * @param userId User ID
     */
    private void clearUserCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElse(null);

        if (cart != null) {
            cartItemRepository.deleteByCartId(cart.getId());
            cart.setDiscountAmount(BigDecimal.ZERO);
            cart.setAppliedCouponId(null);
            cartRepository.save(cart);
            logger.info("Cart cleared for user ID: {}", userId);
        }
    }

    /**
     * Get payment details for an order
     * 
     * @param orderId Order ID
     * @return Payment entity
     */
    public Payment getPayment(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found for order ID: " + orderId));
    }

}
