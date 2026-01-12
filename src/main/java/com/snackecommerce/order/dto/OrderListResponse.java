package com.snackecommerce.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response for order list view
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderListResponse {
    private Long id;
    private String orderNumber;
    private String status;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private String trackingNumber;
    private Integer itemCount;  // Number of items in order
    private String receiverName;  // Recipient name
    private LocalDateTime deliveredAt;  // Delivery date
}
