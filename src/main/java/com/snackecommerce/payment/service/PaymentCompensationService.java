package com.snackecommerce.payment.service;

import com.snackecommerce.order.entity.Order;
import com.snackecommerce.order.entity.OrderItem;
import com.snackecommerce.order.enums.OrderStatus;
import com.snackecommerce.order.repository.OrderItemRepository;
import com.snackecommerce.order.repository.OrderRepository;
import com.snackecommerce.payment.entity.Payment;
import com.snackecommerce.payment.enums.PaymentStatus;
import com.snackecommerce.payment.repository.PaymentRepository;
import com.snackecommerce.payment.util.RazorpayUtil;
import com.snackecommerce.product.entity.Product;
import com.snackecommerce.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Payment Compensation Service
 * 
 * Handles refund operations in SEPARATE TRANSACTIONS
 * This ensures compensations persist even if main transaction rolls back
 * 
 * ✅ CRITICAL: This is a separate @Service so Spring proxy applies @Transactional
 * ✅ Self-invocation problem solved by using external bean
 */
@Service
public class PaymentCompensationService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentCompensationService.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private RazorpayUtil razorpayUtil;

    /**
     * Handle refund for stock conflict in SEPARATE TRANSACTION
     * 
     * @Transactional(propagation = Propagation.REQUIRES_NEW) creates new transaction
     * This ensures all operations (DB + refund) are atomic
     * 
     * Operations (in order):
     * 1. Release stock reservation
     * 2. Mark order as CANCELLED
     * 3. Mark payment as FAILED
     * 4. Initiate Razorpay refund (LAST)
     * 
     * Why refund is LAST:
     * - If DB operations fail → rollback entire transaction (refund never called)
     * - If refund fails → rollback entire transaction (DB reverted)
     * - If both succeed → transaction commits
     * - Scheduler handles any orphaned PAYMENT_PENDING orders
     * 
     * @param order Order to cancel
     * @param payment Payment to refund
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleStockConflictRefund(Order order, Payment payment) {
        try {
            logger.info("Handling stock conflict refund in SEPARATE transaction for order: {}", order.getId());
            
            // 1. Release stock reservation (DB operation)
            releaseStockReservation(order.getId());
            
            // 2. Update order status (DB operation)
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
            
            // 3. Update payment status (DB operation)
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            
            // 4. Refund LAST (after all DB operations succeed)
            // If this fails, entire transaction rolls back (including DB changes)
            refundPayment(payment, "Item sold out - stock unavailable due to concurrent order");
            
            logger.info("Stock conflict refund transaction committed for order: {}", order.getId());
        } catch (Exception e) {
            logger.error("CRITICAL: Compensation failed - rolling back all changes. Order: {}", order.getId(), e);
            throw new RuntimeException("Compensation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Handle refund for coupon validation failure in SEPARATE TRANSACTION
     * 
     * @Transactional(propagation = Propagation.REQUIRES_NEW) creates new transaction
     * This ensures all operations (DB + refund) are atomic
     * 
     * Operations (in order):
     * 1. Release stock reservation
     * 2. Mark order as CANCELLED
     * 3. Mark payment as FAILED
     * 4. Initiate Razorpay refund (LAST)
     * 
     * Why refund is LAST:
     * - If DB operations fail → rollback entire transaction (refund never called)
     * - If refund fails → rollback entire transaction (DB reverted)
     * - If both succeed → transaction commits
     * - Scheduler handles any orphaned PAYMENT_PENDING orders
     * 
     * @param order Order to cancel
     * @param payment Payment to refund
     * @param validationException Exception from coupon validation
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleCouponValidationRefund(Order order, Payment payment, RuntimeException validationException) {
        try {
            logger.info("Handling coupon validation refund in SEPARATE transaction for order: {}", order.getId());
            
            // 1. Release stock reservation (DB operation)
            releaseStockReservation(order.getId());
            
            // 2. Update order status (DB operation)
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
            
            // 3. Update payment status (DB operation)
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            
            // 4. Refund LAST (after all DB operations succeed)
            // If this fails, entire transaction rolls back (including DB changes)
            refundPayment(payment, "Coupon validation failed: " + validationException.getMessage());
            
            logger.info("Coupon validation refund transaction committed for order: {}", order.getId());
        } catch (Exception e) {
            logger.error("CRITICAL: Compensation failed - rolling back all changes. Order: {}", order.getId(), e);
            throw new RuntimeException("Compensation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Refund payment via Razorpay
     * 
     * @param payment Payment to refund
     * @param reason Reason for refund
     */
    private void refundPayment(Payment payment, String reason) {
        try {
            razorpayUtil.refundPayment(payment.getProviderPaymentId(), reason);
            logger.info("Refund initiated for payment ID: {} - Reason: {}", payment.getProviderPaymentId(), reason);
        } catch (Exception e) {
            logger.error("Failed to initiate refund for payment ID: {}", payment.getProviderPaymentId(), e);
            throw new RuntimeException("Refund initiation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Release stock reservation for all items in order
     * 
     * @param orderId Order ID
     */
    @Transactional
    protected void releaseStockReservation(Long orderId) {
        try {
            logger.info("Releasing stock reservation for order ID: {}", orderId);
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
            // Implementation would fetch order items and release reservations
            // For now, this is a placeholder - actual logic in PaymentService
        } catch (Exception e) {
            logger.error("Failed to release stock reservation for order ID: {}", orderId, e);
            throw new RuntimeException("Stock release failed: " + e.getMessage(), e);
        }
    }
}
