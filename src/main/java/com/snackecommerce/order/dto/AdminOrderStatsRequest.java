package com.snackecommerce.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Request parameters for filtering admin order statistics
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminOrderStatsRequest {
    
    // Date filters
    private LocalDate startDate;
    private LocalDate endDate;
    
    // Status filter (optional)
    private String status;
    
    // Include detailed breakdowns
    private Boolean includeRecentOrders;
    private Boolean includeTopProducts;
    private Boolean includeAllOrders;
    
    // Pagination for recent orders
    private Integer recentOrdersLimit;
}
