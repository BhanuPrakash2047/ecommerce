package com.snackecommerce.product.entity;
//
//import com.snackecommerce.cart.entity.Cart;
//import jakarta.persistence.*;
//import lombok.*;
//
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "cart_coupons")
//@Getter @Setter
//@NoArgsConstructor @AllArgsConstructor @Builder
//public class CartCoupon {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @ManyToOne
//    @JoinColumn(name = "cart_id", nullable = false)
//    private Cart cart;
//
//    @ManyToOne
//    @JoinColumn(name = "coupon_id", nullable = false)
//    private Coupon coupon;
//
//    private Double discountAmount;  // Calculated discount (₹ or %)
//
//    private LocalDateTime appliedAt = LocalDateTime.now();
//
//}
