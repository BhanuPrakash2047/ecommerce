package com.snackecommerce.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Complete order response with all details including items, pricing, and discount information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse implements Serializable {
    
    private Long id;
    private String orderNumber;
    private String status;
    
    // Order items
    private List<OrderItemResponse> items;
    
    // Pricing details
    private BigDecimal subtotal;           // Sum of all item prices
    private BigDecimal discountAmount;     // Total discount applied
    private BigDecimal totalAmount;        // Final amount to pay
    
    // Coupon information
    private Long appliedCouponId;
    private String couponCode;             // If coupon is applied
    private String discountType;           // FLAT, PERCENTAGE
    private BigDecimal discountValue;      // Discount value
    
    // Delivery information (address reference)
    private Long addressId;
    private String receiverName;
    private String receiverPhone;
    private String receiverEmail;
    
    // Tracking information
    private String trackingNumber;
    private String trackingAgent;
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deliveredAt;
}
