package com.snackecommerce.product.repository;

//import com.snackecommerce.product.entity.CouponUsage;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;

//@Repository
//public interface CouponUsageRepository extends JpaRepository<CouponUsage, Long> {
//    List<CouponUsage> findByUserId(Long userId);
//
//    List<CouponUsage> findByCouponId(Long couponId);
//
//    @Query("SELECT COUNT(cu) FROM CouponUsage cu WHERE cu.user.id = ?1 AND cu.coupon.id = ?2")
//    Long countUserCouponUsage(Long userId, Long couponId);
//
//    @Query("SELECT SUM(cu.discountApplied) FROM CouponUsage cu WHERE cu.coupon.id = ?1")
//    Double getTotalDiscountByCoupon(Long couponId);
//}
