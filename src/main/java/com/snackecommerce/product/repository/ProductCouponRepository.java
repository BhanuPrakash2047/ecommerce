package com.snackecommerce.product.repository;

import com.snackecommerce.product.entity.ProductCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductCouponRepository extends JpaRepository<ProductCoupon, Long> {

    // Check if specific product is linked to specific coupon
    Optional<ProductCoupon> findByProductIdAndCouponId(Long productId, Long couponId);

    // Get all coupons for a specific product
    List<ProductCoupon> findByProductId(Long productId);

    // Get all products for a specific coupon
    List<ProductCoupon> findByCouponId(Long couponId);

    // Get active coupons for a product (coupon not expired and not deactivated)
    @Query("""
            SELECT pc FROM ProductCoupon pc
            WHERE pc.product.id = :productId
            AND pc.coupon.active = true
            AND pc.coupon.validTill > CURRENT_TIMESTAMP
            ORDER BY pc.linkedAt DESC
            """)
    List<ProductCoupon> getActiveCouponsForProduct(@Param("productId") Long productId);

    // Count total products linked to a coupon
    Long countByCouponId(Long couponId);

    // Check if product exists in any coupon
    boolean existsByProductId(Long productId);

    // Check if coupon is linked to any product
    boolean existsByCouponId(Long couponId);

    // Delete all coupons linked to a product (for cascade handling)
    void deleteByProductId(Long productId);

    // Delete all products linked to a coupon (for cascade handling)
    void deleteByCouponId(Long couponId);
}
