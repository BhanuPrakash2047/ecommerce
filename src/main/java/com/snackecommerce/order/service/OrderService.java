package com.snackecommerce.order.service;

import com.snackecommerce.common.exception.OrderNotFoundException;
import com.snackecommerce.delivery.dto.TrackingResponse;
import com.snackecommerce.delivery.service.DeliveryService;
import com.snackecommerce.order.dto.AdminOrderStatsRequest;
import com.snackecommerce.order.dto.AdminOrderStatsResponse;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
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

        @Autowired
        private DeliveryService deliveryService;



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
                ensureOrderAccess(order, userEmail);
        return mapToResponse(order);
    }

        /**
         * Track order delivery status for the authenticated user.
         */
        public TrackingResponse trackOrder(Long orderId, String userEmail) throws Exception {
                Order order = getOrderById(orderId);
                ensureOrderAccess(order, userEmail);
                return deliveryService.trackOrder(orderId);
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

        // Pending deliveries
        long paymentDone = allOrders.stream()
                .filter(o -> o.getStatus().equals(OrderStatus.PAYMENT_PENDING))
                .count();
        analytics.put("paymentDone", paymentDone);
        
        // Delivered today
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        long deliveredToday = allOrders.stream()
                .filter(o -> o.getDeliveredAt() != null && o.getDeliveredAt().isAfter(today))
                .count();
        analytics.put("deliveredToday", deliveredToday);
        
        return analytics;
    }

        private void ensureOrderAccess(Order order, String userEmail) {
                if (userEmail == null || userEmail.isBlank()) {
                        return;
                }

                User user = userRepository.findByEmail(userEmail)
                                .orElseThrow(() -> new OrderNotFoundException("User not found: " + userEmail));

                if (!order.getUserId().equals(user.getId())) {
                        throw new AccessDeniedException("You don't have permission to access this order");
                }
        }

    /**
     * Get comprehensive admin order statistics with optional filters
     */
    public AdminOrderStatsResponse getAdminOrderStats(AdminOrderStatsRequest request) {
        // Determine date range
        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;
        
        if (request != null && request.getStartDate() != null) {
            startDateTime = request.getStartDate().atStartOfDay();
        }
        if (request != null && request.getEndDate() != null) {
            endDateTime = request.getEndDate().atTime(LocalTime.MAX);
        }
        
        // Get orders based on filters
        List<Order> orders;
        if (startDateTime != null && endDateTime != null) {
            orders = orderRepository.findByCreatedAtBetween(startDateTime, endDateTime);
        } else if (startDateTime != null) {
            orders = orderRepository.findByCreatedAtAfter(startDateTime);
        } else {
            orders = orderRepository.findAll();
        }
        
        // Filter by status if specified
        if (request != null && request.getStatus() != null && !request.getStatus().isEmpty()) {
            try {
                OrderStatus statusFilter = OrderStatus.valueOf(request.getStatus().toUpperCase());
                orders = orders.stream()
                        .filter(o -> o.getStatus().equals(statusFilter))
                        .toList();
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid status filter: {}", request.getStatus());
            }
        }
        
        // Calculate basic statistics
        long totalOrders = orders.size();
        
        BigDecimal totalRevenue = orders.stream()
                .map(o -> BigDecimal.valueOf(o.getTotalAmount() != null ? o.getTotalAmount() : 0))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal averageOrderValue = totalOrders > 0 
                ? totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        
        // Calculate total items sold
        long totalItemsSold = orders.stream()
                .mapToLong(o -> orderItemRepository.findByOrderId(o.getId()).stream()
                        .mapToLong(OrderItem::getQuantity)
                        .sum())
                .sum();
        
        // Orders by status
        Map<String, Long> ordersByStatus = new LinkedHashMap<>();
        Map<String, BigDecimal> revenueByStatus = new LinkedHashMap<>();
        for (OrderStatus status : OrderStatus.values()) {
            long count = orders.stream()
                    .filter(o -> o.getStatus().equals(status))
                    .count();
            ordersByStatus.put(status.toString(), count);
            
            BigDecimal statusRevenue = orders.stream()
                    .filter(o -> o.getStatus().equals(status))
                    .map(o -> BigDecimal.valueOf(o.getTotalAmount() != null ? o.getTotalAmount() : 0))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            revenueByStatus.put(status.toString(), statusRevenue);
        }
        
        // Time-based statistics
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfToday = now.toLocalDate().atStartOfDay();
        LocalDateTime startOfWeek = now.toLocalDate().with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY)).atStartOfDay();
        LocalDateTime startOfMonth = now.toLocalDate().with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay();
        
        // Orders today/week/month
        long ordersToday = orders.stream()
                .filter(o -> o.getCreatedAt() != null && o.getCreatedAt().isAfter(startOfToday))
                .count();
        long ordersThisWeek = orders.stream()
                .filter(o -> o.getCreatedAt() != null && o.getCreatedAt().isAfter(startOfWeek))
                .count();
        long ordersThisMonth = orders.stream()
                .filter(o -> o.getCreatedAt() != null && o.getCreatedAt().isAfter(startOfMonth))
                .count();
        
        // Revenue today/week/month
        BigDecimal revenueToday = orders.stream()
                .filter(o -> o.getCreatedAt() != null && o.getCreatedAt().isAfter(startOfToday))
                .map(o -> BigDecimal.valueOf(o.getTotalAmount() != null ? o.getTotalAmount() : 0))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal revenueThisWeek = orders.stream()
                .filter(o -> o.getCreatedAt() != null && o.getCreatedAt().isAfter(startOfWeek))
                .map(o -> BigDecimal.valueOf(o.getTotalAmount() != null ? o.getTotalAmount() : 0))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal revenueThisMonth = orders.stream()
                .filter(o -> o.getCreatedAt() != null && o.getCreatedAt().isAfter(startOfMonth))
                .map(o -> BigDecimal.valueOf(o.getTotalAmount() != null ? o.getTotalAmount() : 0))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Delivery statistics
        long pendingDeliveries = orders.stream()
                .filter(o -> o.getStatus().equals(OrderStatus.SHIPPED) || o.getStatus().equals(OrderStatus.CONFIRMED))
                .count();
        long deliveredToday = orders.stream()
                .filter(o -> o.getDeliveredAt() != null && o.getDeliveredAt().isAfter(startOfToday))
                .count();
        long deliveredThisWeek = orders.stream()
                .filter(o -> o.getDeliveredAt() != null && o.getDeliveredAt().isAfter(startOfWeek))
                .count();
        
        // Payment statistics
        long paymentPending = orders.stream()
                .filter(o -> o.getStatus().equals(OrderStatus.PAYMENT_PENDING) || o.getStatus().equals(OrderStatus.CREATED))
                .count();
        long paymentCompleted = orders.stream()
                .filter(o -> o.getStatus().equals(OrderStatus.PAID) || 
                        o.getStatus().equals(OrderStatus.CONFIRMED) ||
                        o.getStatus().equals(OrderStatus.SHIPPED) ||
                        o.getStatus().equals(OrderStatus.DELIVERED))
                .count();
        
        // Cancellation/Return statistics
        long cancelledOrders = orders.stream()
                .filter(o -> o.getStatus().equals(OrderStatus.CANCELLED))
                .count();
        long returnedOrders = orders.stream()
                .filter(o -> o.getStatus().equals(OrderStatus.RETURNED))
                .count();
        BigDecimal cancelledOrdersValue = orders.stream()
                .filter(o -> o.getStatus().equals(OrderStatus.CANCELLED))
                .map(o -> BigDecimal.valueOf(o.getTotalAmount() != null ? o.getTotalAmount() : 0))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal returnedOrdersValue = orders.stream()
                .filter(o -> o.getStatus().equals(OrderStatus.RETURNED))
                .map(o -> BigDecimal.valueOf(o.getTotalAmount() != null ? o.getTotalAmount() : 0))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Recent orders
        List<OrderListResponse> recentOrders = null;
        if (request == null || request.getIncludeRecentOrders() == null || request.getIncludeRecentOrders()) {
            int limit = (request != null && request.getRecentOrdersLimit() != null) 
                    ? request.getRecentOrdersLimit() : 10;
            Pageable pageable = PageRequest.of(0, limit);
            
            List<Order> recent;
            if (startDateTime != null && endDateTime != null) {
                recent = orderRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(startDateTime, endDateTime, pageable);
            } else {
                recent = orderRepository.findAllByOrderByCreatedAtDesc(pageable);
            }
            recentOrders = recent.stream()
                    .map(this::mapToListResponse)
                    .toList();
        }
        
        // All filtered orders (when requested)
        List<OrderListResponse> allOrdersList = null;
        if (request != null && Boolean.TRUE.equals(request.getIncludeAllOrders())) {
            allOrdersList = orders.stream()
                    .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                    .map(this::mapToListResponse)
                    .toList();
        }
        
        // Top products by quantity sold
        List<AdminOrderStatsResponse.TopProductStats> topProducts = null;
        if (request != null && Boolean.TRUE.equals(request.getIncludeTopProducts())) {
            topProducts = calculateTopProducts(orders, 10);
        }
        
        // Get filter status string
        String filterStatus = (request != null && request.getStatus() != null && !request.getStatus().isEmpty()) 
                ? request.getStatus().toUpperCase() : null;
        
        return AdminOrderStatsResponse.builder()
                .totalOrders(totalOrders)
                .totalRevenue(totalRevenue)
                .averageOrderValue(averageOrderValue)
                .totalItemsSold(totalItemsSold)
                .ordersByStatus(ordersByStatus)
                .revenueByStatus(revenueByStatus)
                .ordersToday(ordersToday)
                .ordersThisWeek(ordersThisWeek)
                .ordersThisMonth(ordersThisMonth)
                .revenueToday(revenueToday)
                .revenueThisWeek(revenueThisWeek)
                .revenueThisMonth(revenueThisMonth)
                .pendingDeliveries(pendingDeliveries)
                .deliveredToday(deliveredToday)
                .deliveredThisWeek(deliveredThisWeek)
                .paymentPending(paymentPending)
                .paymentCompleted(paymentCompleted)
                .cancelledOrders(cancelledOrders)
                .returnedOrders(returnedOrders)
                .cancelledOrdersValue(cancelledOrdersValue)
                .returnedOrdersValue(returnedOrdersValue)
                .recentOrders(recentOrders)
                .allOrders(allOrdersList)
                .topProducts(topProducts)
                .filterStartDate(startDateTime)
                .filterEndDate(endDateTime)
                .filterStatus(filterStatus)
                .build();
    }
    
    /**
     * Calculate top selling products from orders
     */
    private List<AdminOrderStatsResponse.TopProductStats> calculateTopProducts(List<Order> orders, int limit) {
        Map<Long, AdminOrderStatsResponse.TopProductStats> productStats = new HashMap<>();
        
        for (Order order : orders) {
            List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
            for (OrderItem item : items) {
                Long productId = item.getProductId();
                AdminOrderStatsResponse.TopProductStats stats = productStats.getOrDefault(productId,
                        AdminOrderStatsResponse.TopProductStats.builder()
                                .productId(productId)
                                .productName(item.getProductNameSnapshot())
                                .quantitySold(0L)
                                .revenue(BigDecimal.ZERO)
                                .build());
                
                stats.setQuantitySold(stats.getQuantitySold() + item.getQuantity());
                stats.setRevenue(stats.getRevenue().add(item.getSubtotal() != null ? item.getSubtotal() : BigDecimal.ZERO));
                productStats.put(productId, stats);
            }
        }
        
        return productStats.values().stream()
                .sorted((a, b) -> Long.compare(b.getQuantitySold(), a.getQuantitySold()))
                .limit(limit)
                .toList();
    }

    /**
     * Get orders filtered by status (admin)
     */
    public List<OrderListResponse> getOrdersByStatus(String status) {
        try {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            return orderRepository.findByStatus(orderStatus).stream()
                    .map(this::mapToListResponse)
                    .toList();
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid status: {}", status);
            return Collections.emptyList();
        }
    }
    
    /**
     * Get orders filtered by date range (admin)
     */
    public List<OrderListResponse> getOrdersByDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        
        return orderRepository.findByCreatedAtBetween(startDateTime, endDateTime).stream()
                .map(this::mapToListResponse)
                .toList();
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
        
        // Count items in order
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        int itemCount = items.size();
        
        return OrderListResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus().toString())
                .totalAmount(totalAmount)
                .createdAt(order.getCreatedAt())
                .trackingNumber(order.getTrackingNumber())
                .itemCount(itemCount)
                .receiverName(order.getReceiverName())
                .deliveredAt(order.getDeliveredAt())
                .build();
    }
}
