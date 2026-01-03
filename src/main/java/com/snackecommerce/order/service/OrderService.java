package com.snackecommerce.order.service;

import com.snackecommerce.cart.entity.Cart;
import com.snackecommerce.common.exception.OrderNotFoundException;
import com.snackecommerce.order.dto.OrderResponse;
import com.snackecommerce.order.dto.OrderListResponse;
import com.snackecommerce.order.dto.OrderItemResponse;
import com.snackecommerce.order.entity.Order;
import com.snackecommerce.order.entity.OrderItem;
import com.snackecommerce.order.enums.OrderStatus;
import com.snackecommerce.order.repository.OrderItemRepository;
import com.snackecommerce.order.repository.OrderRepository;
import com.snackecommerce.product.entity.Coupon;
import com.snackecommerce.product.repository.CouponRepository;
import com.snackecommerce.user.entity.User;
import com.snackecommerce.user.repository.UserRepository;
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
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CouponRepository couponRepository;



    /**
     * Get all orders for a user by email (for controller)
     */
    public List<OrderListResponse> getUserOrdersByEmail(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new OrderNotFoundException("User not found: " + userEmail));
        return orderRepository.findByUserId(user.getId()).stream()
                .map(this::mapToListResponse)
                .toList();
    }

    /**
     * Get order by ID
     */
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));
    }

    /**
     * Get order details with user authorization check
     */
    public OrderResponse getOrderDetails(Long orderId, String userEmail) {
        Order order = getOrderById(orderId);
        // TODO: Verify user owns this order
        return mapToResponse(order);
    }



    /**
     * Get all orders with list response (admin only)
     */
    public List<OrderListResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::mapToListResponse)
                .toList();
    }


    /**
     * Get order analytics for admin dashboard
     */
    public Map<String, Object> getOrderAnalytics() {
        Map<String, Object> analytics = new HashMap<>();
        
        List<Order> allOrders = orderRepository.findAll();
        
        // Total orders
        analytics.put("totalOrders", allOrders.size());
        
        // Orders by status
        Map<String, Long> statusCount = new HashMap<>();
        for (OrderStatus status : OrderStatus.values()) {
            statusCount.put(status.toString(), 
                    allOrders.stream().filter(o -> o.getStatus().equals(status)).count());
        }
        analytics.put("ordersByStatus", statusCount);
        
        // Total revenue
        BigDecimal totalRevenue = allOrders.stream()
                .map(o -> BigDecimal.valueOf(o.getTotalAmount() != null ? o.getTotalAmount() : 0))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        analytics.put("totalRevenue", totalRevenue);
        
        // Pending deliveries
        long pendingDeliveries = allOrders.stream()
                .filter(o -> o.getStatus().equals(OrderStatus.SHIPPED) || o.getStatus().equals(OrderStatus.CONFIRMED))
                .count();
        analytics.put("pendingDeliveries", pendingDeliveries);
        
        // Delivered today
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        long deliveredToday = allOrders.stream()
                .filter(o -> o.getDeliveredAt() != null && o.getDeliveredAt().isAfter(today))
                .count();
        analytics.put("deliveredToday", deliveredToday);
        
        return analytics;
    }

    // ==================== PRIVATE HELPER METHODS ====================

    /**
     * Map Order entity to OrderResponse DTO with complete details
     */
    private OrderResponse mapToResponse(Order order) {
        // Get all order items
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
        List<OrderItemResponse> itemResponses = orderItems.stream()
                .map(this::mapToOrderItemResponse)
                .toList();
        
        // Get coupon details if applied
        String couponCode = null;
        String discountType = null;
        BigDecimal discountValue = BigDecimal.ZERO;
        
        if (order.getAppliedCouponId() != null) {
            Optional<Coupon> coupon = couponRepository.findById(order.getAppliedCouponId());
            if (coupon.isPresent()) {
                Coupon c = coupon.get();
                couponCode = c.getCode();
                discountType = c.getType().toString();
                discountValue = c.getDiscountValue() != null ? c.getDiscountValue() : BigDecimal.ZERO;
            }
        }
        
        // Use BigDecimal amounts if available, otherwise convert from Double
        BigDecimal subtotal = order.getSubtotal() != null ? order.getSubtotal() :
                itemResponses.stream()
                        .map(OrderItemResponse::getSubtotal)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal discountAmount = order.getDiscountAmount() != null ? 
                order.getDiscountAmount() : BigDecimal.ZERO;
        
        BigDecimal totalAmount = order.getTotalAmountBigDecimal() != null ? 
                order.getTotalAmountBigDecimal() : 
                BigDecimal.valueOf(order.getTotalAmount() != null ? order.getTotalAmount() : 0);
        
        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus().toString())
                // Items
                .items(itemResponses)
                // Pricing
                .subtotal(subtotal)
                .discountAmount(discountAmount)
                .totalAmount(totalAmount)
                // Coupon info
                .appliedCouponId(order.getAppliedCouponId())
                .couponCode(couponCode)
                .discountType(discountType)
                .discountValue(discountValue)
                // Delivery info (address reference)
                .addressId(order.getAddressId())
                .receiverName(order.getReceiverName())
                .receiverPhone(order.getReceiverPhone())
                .receiverEmail(order.getReceiverEmail())
                // Tracking
                .trackingNumber(order.getTrackingNumber())
                .trackingAgent(order.getTrackingAgent() != null ? order.getTrackingAgent().toString() : null)
                // Timestamps
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .deliveredAt(order.getDeliveredAt())
                .build();
    }

    /**
     * Map OrderItem to OrderItemResponse
     */
    private OrderItemResponse mapToOrderItemResponse(OrderItem item) {
        return OrderItemResponse.builder()
                .itemId(item.getId())
                .productId(item.getProductId())
                .productName(item.getProductNameSnapshot())
                .unitPrice(item.getUnitPrice())
                .quantity(item.getQuantity())
                .subtotal(item.getSubtotal())
                .build();
    }

    /**
     * Map Order entity to OrderListResponse DTO
     */
    private OrderListResponse mapToListResponse(Order order) {
        BigDecimal totalAmount = order.getTotalAmount() != null ? 
                BigDecimal.valueOf(order.getTotalAmount()) : BigDecimal.ZERO;
        return OrderListResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus().toString())
                .totalAmount(totalAmount)
                .createdAt(order.getCreatedAt())
                .trackingNumber(order.getTrackingNumber())
                .build();
    }
}
