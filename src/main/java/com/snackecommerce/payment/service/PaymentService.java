package com.snackecommerce.payment.service;

import com.snackecommerce.cart.entity.Cart;
import com.snackecommerce.cart.repository.CartItemRepository;
import com.snackecommerce.cart.repository.CartRepository;
import com.snackecommerce.common.exception.CartNotFoundException;
import com.snackecommerce.order.entity.Order;
import com.snackecommerce.order.enums.OrderStatus;
import com.snackecommerce.order.repository.OrderItemRepository;
import com.snackecommerce.order.repository.OrderRepository;
import com.snackecommerce.order.service.ShipmentJobService;
import com.snackecommerce.payment.dto.CreatePaymentRequest;
import com.snackecommerce.payment.dto.PaymentResponse;
import com.snackecommerce.payment.entity.Payment;
import com.snackecommerce.payment.enums.PaymentProvider;
import com.snackecommerce.payment.enums.PaymentStatus;
import com.snackecommerce.payment.repository.PaymentRepository;
import com.snackecommerce.payment.util.RazorpayUtil;
import com.snackecommerce.product.entity.Coupon;
import com.snackecommerce.product.repository.CouponRepository;
import com.snackecommerce.delivery.service.DeliveryService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private RazorpayUtil razorpayUtil;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private DeliveryService deliveryService;

    @Autowired
    private ShipmentJobService shipmentJobService;

    /**
     * Create payment order for checkout
     */
    @Transactional
    public PaymentResponse createPayment(CreatePaymentRequest request) throws Exception {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new CartNotFoundException("Order not found with ID: " + request.getOrderId()));

        if (!order.getStatus().equals(OrderStatus.PAYMENT_PENDING)) {
            throw new RuntimeException("Invalid order status for payment: " + order.getStatus());
        }

        logger.info("Creating payment for order ID: {}, amount: {} INR", request.getOrderId(), request.getAmount());

        try {
            JSONObject razorpayOrder = razorpayUtil.createOrder(
                request.getAmount(),
                order.getId().toString(),
                request.getEmail(),
                request.getPhone()
            );

            String razorpayOrderId = razorpayOrder.getString("id");

            Payment payment = Payment.builder()
                    .orderId(order.getId())
                    .amount(request.getAmount().doubleValue())
                    .provider(PaymentProvider.RAZORPAY)
                    .status(PaymentStatus.INITIATED)
                    .providerOrderId(razorpayOrderId)
                    .idempotencyKey(order.getId().toString())
                    .build();

            paymentRepository.save(payment);
            logger.info("Payment record created for Razorpay order: {}", razorpayOrderId);

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
     */
    @Transactional
    public void handlePaymentSuccess(String razorpayOrderId, String razorpayPaymentId, String signature) throws Exception {
        logger.info("Processing payment success webhook for Razorpay order: {}", razorpayOrderId);

        if (!razorpayUtil.verifyWebhookSignature(razorpayOrderId, razorpayPaymentId, signature)) {
            logger.error("Webhook signature verification failed for order: {}", razorpayOrderId);
            throw new RuntimeException("Invalid webhook signature");
        }

        Optional<Payment> paymentOptional = paymentRepository.findByProviderOrderId(razorpayOrderId);
        if (paymentOptional.isEmpty()) {
            logger.warn("Payment not found for Razorpay order ID: {}", razorpayOrderId);
            throw new RuntimeException("Payment not found for order ID: " + razorpayOrderId);
        }

        Payment payment = paymentOptional.get();

        if (payment.getStatus().equals(PaymentStatus.SUCCESS)) {
            logger.info("Payment already processed for order: {}", razorpayOrderId);
            return;
        }

        Order order = orderRepository.findById(payment.getOrderId())
                .orElseThrow(() -> new CartNotFoundException("Order not found"));


        // All validations passed
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setProviderPaymentId(razorpayPaymentId);
        payment.setConfirmedAt(LocalDateTime.now());
        paymentRepository.save(payment);
        logger.info("Payment status updated to SUCCESS for order: {}", payment.getOrderId());

        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);
        logger.info("Order status updated to CONFIRMED for order ID: {}", order.getId());

        // Clear cart
        clearUserCart(order.getUserId());
        logger.info("Cart cleared for user ID: {}", order.getUserId());

        // Create shipment on Delhivery
        try {
            String waybill = deliveryService.createShipment(order.getId());
            logger.info("Shipment created successfully for order ID: {} with waybill: {}", order.getId(), waybill);
        } catch (Exception e) {
            logger.error("Failed to create shipment for order ID: {}. Saving to retry queue.", order.getId(), e);
            // Save failed shipment as a retry job - will be retried automatically by scheduler
            shipmentJobService.saveFailedShipment(order.getId(), e.getMessage());
            logger.info("Shipment job created for order {} - will retry automatically", order.getId());
        }
    }

    /**
     * Handle failed payment webhook
     */
    @Transactional
    public void handlePaymentFailure(String razorpayOrderId, String razorpayPaymentId) throws Exception {
        logger.info("Processing payment failure webhook for Razorpay order: {}", razorpayOrderId);

        Optional<Payment> paymentOptional = paymentRepository.findByProviderOrderId(razorpayOrderId);
        if (paymentOptional.isEmpty()) {
            logger.warn("Payment not found for failed Razorpay order ID: {}", razorpayOrderId);
            throw new RuntimeException("Payment not found for order ID: " + razorpayOrderId);
        }

        Payment payment = paymentOptional.get();

        if (payment.getStatus().equals(PaymentStatus.FAILED)) {
            logger.info("Payment failure already processed for order: {}", razorpayOrderId);
            return;
        }

        Order order = orderRepository.findById(payment.getOrderId())
                .orElseThrow(() -> new CartNotFoundException("Order not found"));

        payment.setStatus(PaymentStatus.FAILED);
        payment.setProviderPaymentId(razorpayPaymentId);
        paymentRepository.save(payment);
        logger.info("Payment status updated to FAILED for order: {}", payment.getOrderId());

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        logger.info("Order status updated to CANCELLED for order ID: {}", order.getId());
    }

    /**
     * Validate coupon for payment (only checks if active and not expired)
     */
    private void validateCouponForPayment(Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new RuntimeException("Coupon not found with ID: " + couponId));

        if (!coupon.getActive()) {
            throw new RuntimeException("Coupon is inactive");
        }

        if (coupon.getValidTill() != null && LocalDateTime.now().isAfter(coupon.getValidTill())) {
            throw new RuntimeException("Coupon has expired");
        }

        logger.info("Coupon validation passed for coupon ID: {}", couponId);
    }


    /**
     * Clear cart items for a user
     */
    private void clearUserCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId).orElse(null);
        if (cart != null) {
            cartItemRepository.deleteByCartId(cart.getId());
            cart.setDiscountAmount(BigDecimal.ZERO);
            cart.setAppliedCouponId(null);
            cartRepository.save(cart);
            logger.info("Cart cleared for user ID: {}", userId);
        }
    }

    /**
     * Manual payment success handler (for admin override when webhook fails)
     * Does NOT verify Razorpay signature - admin manually verifies in Razorpay dashboard
     * 
     * @param orderId Order ID
     * @throws Exception if order/payment not found
     */
    @Transactional
    public void manualMarkPaymentSuccess(Long orderId) throws Exception {
        logger.info("Admin: Manually marking payment as SUCCESS for order ID: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CartNotFoundException("Order not found with ID: " + orderId));

        // Find payment record for this order
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found for order ID: " + orderId));

        if (payment.getStatus().equals(PaymentStatus.SUCCESS)) {
            logger.info("Payment already marked SUCCESS for order: {}", orderId);
            return;
        }

        // Mark payment as success
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setConfirmedAt(LocalDateTime.now());
        paymentRepository.save(payment);
        logger.info("Payment manually marked SUCCESS for order: {}", orderId);

        // Mark order as confirmed
        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);
        logger.info("Order manually marked CONFIRMED for order ID: {}", orderId);

        // Clear cart
        clearUserCart(order.getUserId());
        logger.info("Cart cleared for user ID: {}", order.getUserId());

        // Create shipment on Delhivery
        try {
            String waybill = deliveryService.createShipment(order.getId());
            logger.info("Shipment created successfully for order ID: {} with waybill: {}", order.getId(), waybill);
        } catch (Exception e) {
            logger.error("Failed to create shipment for order ID: {}. Saving to retry queue.", order.getId(), e);
            // Save failed shipment as a retry job - will be retried automatically by scheduler
            shipmentJobService.saveFailedShipment(order.getId(), e.getMessage());
            logger.info("Shipment job created for order {} - will retry automatically", order.getId());
        }
    }

    /**
     * Manual payment failure handler (for admin override when webhook fails)
     * Does NOT verify anything - admin manually confirms payment failed in Razorpay dashboard
     * 
     * @param orderId Order ID
     * @throws Exception if order/payment not found
     */
    @Transactional
    public void manualMarkPaymentFailed(Long orderId) throws Exception {
        logger.info("Admin: Manually marking payment as FAILED for order ID: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CartNotFoundException("Order not found with ID: " + orderId));

        // Find payment record for this order
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found for order ID: " + orderId));

        if (payment.getStatus().equals(PaymentStatus.FAILED)) {
            logger.info("Payment already marked FAILED for order: {}", orderId);
            return;
        }

        // Mark payment as failed
        payment.setStatus(PaymentStatus.FAILED);
        paymentRepository.save(payment);
        logger.info("Payment manually marked FAILED for order: {}", orderId);

        // Mark order as cancelled
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        logger.info("Order manually marked CANCELLED for order ID: {}", orderId);
    }

}
