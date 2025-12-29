package com.snackecommerce.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemResponse {
    private Long cartItemId;
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal snapshotPrice;      // Price when added
    private BigDecimal currentPrice;       // Current price from DB
    private BigDecimal itemTotal;          // quantity × currentPrice
    private String priceChangeAlert;       // Alert if price changed
}
