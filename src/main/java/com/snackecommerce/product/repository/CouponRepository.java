package com.snackecommerce.product.repository;

import java.util.Optional;

public interface CouponRepository extends org.springframework.data.jpa.repository.JpaRepository<com.snackecommerce.product.entity.Coupon, Long> {
    Optional<Object> findByCode(String code);
}
