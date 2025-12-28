package com.snackecommerce.product.service;

import com.snackecommerce.common.exception.*;
import com.snackecommerce.product.dto.*;
import com.snackecommerce.product.entity.Coupon;
import com.snackecommerce.product.entity.Product;
import com.snackecommerce.product.entity.ProductCoupon;
import com.snackecommerce.product.enums.DiscountType;
import com.snackecommerce.product.repository.CouponRepository;
import com.snackecommerce.product.repository.ProductCouponRepository;
import com.snackecommerce.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CouponService {

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private ProductCouponRepository productCouponRepository;

    @Autowired
    private ProductRepository productRepository;

    // ==================== ADMIN OPERATIONS ====================

    public CouponResponse createCoupon(CouponRequest request) {
        // Validate dates
        if (request.getValidFrom().isAfter(request.getValidTill())) {
            throw new InvalidCouponStateException("Valid from date must be before valid till date");
        }

        // Check if coupon code already exists
        if (couponRepository.findByCode(request.getCode()).isPresent()) {
            throw new InvalidCouponStateException("Coupon code already exists: " + request.getCode());
        }

        Coupon coupon = Coupon.builder()
                .code(request.getCode().toUpperCase())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .minOrderAmount(request.getMinOrderAmount() != null ? request.getMinOrderAmount() : 0.0)
                .maxUsagePerUser(request.getMaxUsagePerUser())
                .totalUsageLimit(request.getTotalUsageLimit())
                .validFrom(request.getValidFrom())
                .validTill(request.getValidTill())
                .active(true)
                .usedCount(0)
                .build();

        coupon = couponRepository.save(coupon);
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

        if (request.getDiscountValue() != null) {
            coupon.setDiscountValue(request.getDiscountValue());
        }
        if (request.getMaxUsagePerUser() != null) {
            coupon.setMaxUsagePerUser(request.getMaxUsagePerUser());
        }
        if (request.getTotalUsageLimit() != null) {
            coupon.setTotalUsageLimit(request.getTotalUsageLimit());
        }
        if (request.getMinOrderAmount() != null) {
            coupon.setMinOrderAmount(request.getMinOrderAmount());
        }

        coupon = couponRepository.save(coupon);
        return mapToResponse(coupon);
    }

    public void deactivateCoupon(Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new CouponNotFoundException("Coupon not found with ID: " + couponId));
        coupon.setActive(false);
        couponRepository.save(coupon);
    }

    public void deleteCoupon(Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new CouponNotFoundException("Coupon not found with ID: " + couponId));

        // ==================== MANUAL CASCADE DELETE ====================
        // Delete all dependent records in correct order to avoid constraint violations

        // 1. Delete product-coupon links
        productCouponRepository.deleteByCouponId(couponId);

        // 2. Delete cart-coupon links (from CartCoupon table)
        // Note: This will be implemented when Cart module is created
        // For now, manually delete via custom repository method if needed

        // 3. Delete coupon usage history
        // Note: This can be kept for audit purposes, or deleted if needed
        // couponUsageRepository.deleteByCouponId(couponId); // Implement if needed

        // 4. Finally delete the coupon
        couponRepository.delete(coupon);
    }

    public Page<CouponResponse> getAllCoupons(int page, int size) {
        return couponRepository.findAll(PageRequest.of(page, size))
                .map(this::mapToResponse);
    }

    // ==================== HELPER METHODS ====================

    private CouponResponse mapToResponse(Coupon coupon) {
        return CouponResponse.builder()
                .id(coupon.getId())
                .code(coupon.getCode())
                .discountType(coupon.getDiscountType())
                .discountValue(coupon.getDiscountValue())
                .minOrderAmount(coupon.getMinOrderAmount())
                .maxUsagePerUser(coupon.getMaxUsagePerUser())
                .totalUsageLimit(coupon.getTotalUsageLimit())
                .usedCount(coupon.getUsedCount())
                .active(coupon.getActive())
                .validFrom(coupon.getValidFrom())
                .validTill(coupon.getValidTill())
                .build();
    }

    // ==================== PRODUCT-COUPON LINKING ====================

    /**
     * Link a coupon to specific products
     * Admin only operation to define which products are eligible for a coupon
     */
    public void linkProductsToCoupon(Long couponId, List<Long> productIds) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new CouponNotFoundException("Coupon not found: " + couponId));

        for (Long productId : productIds) {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ProductNotFoundException("Product not found: " + productId));

            // Check if already linked
            if (productCouponRepository.findByProductIdAndCouponId(productId, couponId).isEmpty()) {
                ProductCoupon link = ProductCoupon.builder()
                        .product(product)
                        .coupon(coupon)
                        .build();
                productCouponRepository.save(link);
            }
        }
    }

    /**
     * Unlink a coupon from a specific product
     */
    public void unlinkProductFromCoupon(Long couponId, Long productId) {
        ProductCoupon link = productCouponRepository.findByProductIdAndCouponId(productId, couponId)
                .orElseThrow(() -> new ProductNotFoundException("Product not linked to this coupon"));
        productCouponRepository.delete(link);
    }

    /**
     * Check if product is eligible for a specific coupon
     */
    public boolean isProductEligibleForCoupon(Long productId, Long couponId) {
        return productCouponRepository.findByProductIdAndCouponId(productId, couponId).isPresent();
    }

    /**
     * Get all active coupons for a product
     */
    public List<CouponResponse> getActiveCouponsForProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + productId));

        return productCouponRepository.getActiveCouponsForProduct(productId)
                .stream()
                .map(pc -> mapToResponse(pc.getCoupon()))
                .toList();
    }

    /**
     * Get all products linked to a coupon
     */
    public List<ProductResponse> getProductsForCoupon(Long couponId, int page, int size) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new CouponNotFoundException("Coupon not found: " + couponId));

        List<ProductCoupon> links = productCouponRepository.findByCouponId(couponId);
        return links.stream()
                .map(pc -> ProductResponse.builder()
                        .id(pc.getProduct().getId())
                        .name(pc.getProduct().getName())
                        .price(pc.getProduct().getPrice())
                        .stockQuantity(pc.getProduct().getStockQuantity())
                        .active(pc.getProduct().getActive())
                        .isEligibleForCoupon(pc.getProduct().getIsEligibleForCoupon())
                        .averageRating(null)
                        .reviewCount(null)
                        .createdAt(pc.getProduct().getCreatedAt())
                        .build()
                )
                .skip((long) page * size)
                .limit(size)
                .collect(Collectors.toList());
    }

    private Double calculateDiscount(Double cartTotal, Coupon coupon) {
        if (coupon.getDiscountType() == DiscountType.PERCENTAGE) {
            return cartTotal * (coupon.getDiscountValue() / 100.0);
        } else {
            // FLAT discount
            return Math.min(coupon.getDiscountValue(), cartTotal);
        }
    }

}
