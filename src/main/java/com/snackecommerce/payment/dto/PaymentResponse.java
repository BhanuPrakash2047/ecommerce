package com.snackecommerce.payment.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentResponse {
    private String razorpayOrderId;     // Razorpay Order ID for frontend checkout
    private Long amount;                 // Amount in rupees
    private String email;                // Customer email
    private String phone;                // Customer phone
    private String orderId;              // Internal order ID
}
