package com.snackecommerce.payment.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class CreatePaymentRequest {
    private Long orderId;      // Internal order ID
    private Long amount;       // Amount in rupees
    private String email;      // Customer email
    private String phone;      // Customer phone
}
