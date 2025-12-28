package com.snackecommerce.product.entity;

//import com.snackecommerce.user.entity.User;
//import jakarta.persistence.*;
//import lombok.*;
//
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "coupon_usage")
//@Getter @Setter
//@NoArgsConstructor @AllArgsConstructor @Builder
//public class CouponUsage {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @ManyToOne
//    @JoinColumn(name = "user_id", nullable = false)
//    private User user;
//
//    @ManyToOne
//    @JoinColumn(name = "coupon_id", nullable = false)
//    private Coupon coupon;
//
//    private Double discountApplied;
//
//    private Long orderId;  // Reference to order
//
//    @Column(updatable = false)
//    private LocalDateTime usedAt = LocalDateTime.now();
//}
