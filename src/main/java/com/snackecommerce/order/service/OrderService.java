package com.snackecommerce.order.service;

import com.snackecommerce.cart.entity.Cart;
import com.snackecommerce.order.entity.Order;
import com.snackecommerce.order.enums.OrderStatus;
import com.snackecommerce.order.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Transactional
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    /**
     * Create a dummy order from a validated cart
     * (Real implementation: integrate with payment gateway in Phase 2)
     */
    public Order createDummyOrder(Cart cart) {
        String orderNumber = "ORD-" + System.currentTimeMillis();

        Order order = Order.builder()
                .orderNumber(orderNumber)
                .userId(cart.getUserId())
                .status(OrderStatus.PAYMENT_PENDING)
                .subtotal(calculateSubtotal(cart))
                .discountAmount(cart.getDiscountAmount())
                .totalAmountBigDecimal(cart.getDiscountAmount() != null ? 
                        calculateSubtotal(cart).subtract(cart.getDiscountAmount()) : 
                        calculateSubtotal(cart))
                .appliedCouponId(cart.getAppliedCouponId())
                .cartId(cart.getId())
                .createdAt(LocalDateTime.now())
                .build();

        return orderRepository.save(order);
    }

    /**
     * Get order by ID
     */
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId).orElse(null);
    }

    /**
     * Update order status
     */
    public Order updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order != null) {
            order.setStatus(status);
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);
        }
        return order;
    }

    /**
     * Helper: Calculate subtotal from cart items
     */
    private BigDecimal calculateSubtotal(Cart cart) {
        // Note: This should be calculated from CartItems
        // For now, return zero (will be calculated in controller)
        return BigDecimal.ZERO;
    }
}
