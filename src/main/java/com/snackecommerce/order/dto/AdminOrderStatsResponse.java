package com.snackecommerce.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Comprehensive admin order statistics response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminOrderStatsResponse {
    
    // Summary statistics
    private Long totalOrders;
    private BigDecimal totalRevenue;
    private BigDecimal averageOrderValue;
    private Long totalItemsSold;
    
    // Order count by status
    private Map<String, Long> ordersByStatus;
    
    // Revenue by status (only for completed/delivered orders)
    private Map<String, BigDecimal> revenueByStatus;
    
    // Time-based statistics
    private Long ordersToday;
    private Long ordersThisWeek;
    private Long ordersThisMonth;
    
    private BigDecimal revenueToday;
    private BigDecimal revenueThisWeek;
    private BigDecimal revenueThisMonth;
    
    // Delivery statistics
    private Long pendingDeliveries;
    private Long deliveredToday;
    private Long deliveredThisWeek;
    
    // Payment statistics
    private Long paymentPending;
    private Long paymentCompleted;
    
    // Cancellation/Return statistics
    private Long cancelledOrders;
    private Long returnedOrders;
    private BigDecimal cancelledOrdersValue;
    private BigDecimal returnedOrdersValue;
    
    // Recent orders (last N orders, configurable)
    private List<OrderListResponse> recentOrders;
    
    // All filtered orders (when includeAllOrders=true)
    private List<OrderListResponse> allOrders;
    
    // Top products (by quantity sold)
    private List<TopProductStats> topProducts;
    
    // Date range applied (if filtered)
    private LocalDateTime filterStartDate;
    private LocalDateTime filterEndDate;
    private String filterStatus;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TopProductStats {
        private Long productId;
        private String productName;
        private Long quantitySold;
        private BigDecimal revenue;
    }
}
