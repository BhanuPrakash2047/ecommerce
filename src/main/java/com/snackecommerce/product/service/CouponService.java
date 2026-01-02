package com.snackecommerce.product.service;

import com.snackecommerce.common.exception.CouponNotFoundException;
import com.snackecommerce.common.exception.InvalidCouponStateException;
import com.snackecommerce.product.dto.CouponRequest;
import com.snackecommerce.product.dto.CouponResponse;
import com.snackecommerce.product.dto.ProductResponse;
import com.snackecommerce.product.entity.Coupon;
import com.snackecommerce.product.entity.Coupon.CouponType;
import com.snackecommerce.product.repository.CouponRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Simplified Coupon Service
 * Handles creation, update, and deletion of coupons with flat or percentage discounts
 */
@Service
@Transactional
public class CouponService {

    private static final Logger logger = LoggerFactory.getLogger(CouponService.class);

    @Autowired
    private CouponRepository couponRepository;

    public CouponResponse createCoupon(CouponRequest request) {
        // Validate date range
        if (request.getValidFrom() != null && request.getValidTill() != null) {
            if (request.getValidFrom().isAfter(request.getValidTill())) {
                throw new InvalidCouponStateException("Valid from date must be before valid till date");
            }
        }

        // Check if code exists
        if (couponRepository.findByCode(request.getCode()).isPresent()) {
            throw new InvalidCouponStateException("Coupon code already exists: " + request.getCode());
        }

        // Validate discount value based on type
        if (request.getType() == CouponType.PERCENTAGE && request.getDiscountValue() > 100) {
            throw new InvalidCouponStateException("Percentage discount cannot exceed 100%");
        }

        Coupon coupon = Coupon.builder()
                .code(request.getCode().toUpperCase())
                .type(request.getType())
                .discountValue(request.getDiscountValue())
                .minOrderAmount(request.getMinOrderAmount() != null ? request.getMinOrderAmount() : 0.0)
                .validFrom(request.getValidFrom())
                .validTill(request.getValidTill())
                .active(true)
                .build();

        coupon = couponRepository.save(coupon);
        String discountDesc = request.getType() == CouponType.FLAT 
            ? "₹" + request.getDiscountValue()
            : request.getDiscountValue() + "%";
        String minAmountDesc = coupon.getMinOrderAmount() > 0 ? " (Min: ₹" + coupon.getMinOrderAmount() + ")" : "";
        logger.info("Coupon created: {} with discount: {}{}", coupon.getCode(), discountDesc, minAmountDesc);
        return mapToResponse(coupon);
    }

    public CouponResponse updateCoupon(Long couponId, CouponRequest request) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new CouponNotFoundException("Coupon not found with ID: " + couponId));

        if (request.getValidFrom() != null && request.getValidTill() != null) {
            if (request.getValidFrom().isAfter(request.getValidTill())) {
                throw new InvalidCouponStateException("Valid from date must be before valid till date");
            }
            coupon.setValidFrom(request.getValidFrom());
            coupon.setValidTill(request.getValidTill());
        }

        if (request.getCode() != null) {
            coupon.setCode(request.getCode().toUpperCase());
        }

        if (request.getType() != null) {
            coupon.setType(request.getType());
        }

        if (request.getDiscountValue() != null) {
            if (request.getType() == CouponType.PERCENTAGE && request.getDiscountValue() > 100) {
                throw new InvalidCouponStateException("Percentage discount cannot exceed 100%");
            }
            coupon.setDiscountValue(request.getDiscountValue());
        }

        if (request.getMinOrderAmount() != null) {
            coupon.setMinOrderAmount(request.getMinOrderAmount());
        }

        if (request.getActive() != null) {
            coupon.setActive(request.getActive());
        }

        coupon = couponRepository.save(coupon);
        logger.info("Coupon updated: {}", coupon.getCode());
        return mapToResponse(coupon);
    }

    public void deactivateCoupon(Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new CouponNotFoundException("Coupon not found with ID: " + couponId));
        coupon.setActive(false);
        couponRepository.save(coupon);
        logger.info("Coupon deactivated: {}", coupon.getCode());
    }

    public void deleteCoupon(Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new CouponNotFoundException("Coupon not found with ID: " + couponId));
        couponRepository.delete(coupon);
        logger.info("Coupon deleted: {}", coupon.getCode());
    }

    public CouponResponse getCoupon(Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new CouponNotFoundException("Coupon not found with ID: " + couponId));
        return mapToResponse(coupon);
    }

    public List<CouponResponse> getAllCoupons() {
        return couponRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<CouponResponse> getActiveCoupons() {
        return couponRepository.findByActiveTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ==================== DISCOUNT CALCULATION ====================

    /**
     * Calculate discount amount based on coupon type and cart total
     * @param coupon The coupon to apply
     * @param cartTotal The total cart amount
     * @return Discount amount in rupees
     */
    public Double calculateDiscount(Coupon coupon, Double cartTotal) {
        if (coupon.getType() == CouponType.FLAT) {
            return coupon.getDiscountValue();
        } else {
            // PERCENTAGE: Calculate percentage of cart total
            return (cartTotal * coupon.getDiscountValue()) / 100;
        }
    }

    /**
     * Validate if coupon can be applied based on minimum order amount
     * @param coupon The coupon to validate
     * @param cartTotal The current cart total
     * @throws InvalidCouponStateException if minimum order amount is not met
     */
    public void validateCouponEligibility(Coupon coupon, Double cartTotal) {
        if (coupon.getMinOrderAmount() != null && cartTotal < coupon.getMinOrderAmount()) {
            throw new InvalidCouponStateException(
                "Minimum order amount of ₹" + coupon.getMinOrderAmount() + " required. Current cart: ₹" + cartTotal
            );
        }
    }

    // Stub methods for product-coupon linking (not implemented in simplified architecture)
    public void linkProductsToCoupon(Long couponId, java.util.List<Long> productIds) {
        logger.info("Product-coupon linking not implemented in simplified architecture");
    }

    public void unlinkProductFromCoupon(Long couponId, Long productId) {
        logger.info("Product-coupon unlinking not implemented in simplified architecture");
    }

    public List<CouponResponse> getActiveCouponsForProduct(Long productId) {
        return getActiveCoupons();
    }

    public List<ProductResponse> getProductsForCoupon(Long couponId) {
        return new java.util.ArrayList<>();
    }

    // ==================== HELPER METHODS ====================

    private CouponResponse mapToResponse(Coupon coupon) {
        return CouponResponse.builder()
                .id(coupon.getId())
                .code(coupon.getCode())
                .type(coupon.getType())
                .discountValue(coupon.getDiscountValue())
                .minOrderAmount(coupon.getMinOrderAmount())
                .active(coupon.getActive())
                .validFrom(coupon.getValidFrom())
                .validTill(coupon.getValidTill())
                .build();
    }
}
