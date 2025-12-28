package com.snackecommerce.product.service;

import com.snackecommerce.common.exception.ProductNotFoundException;
import com.snackecommerce.product.dto.ProductRequest;
import com.snackecommerce.product.dto.ProductResponse;
import com.snackecommerce.product.entity.Product;
import com.snackecommerce.product.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private FAQRepository faqRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    @Autowired
    private ProductVideoRepository productVideoRepository;

    @Autowired
    private ProductCouponRepository productCouponRepository;

    public ProductResponse createProduct(ProductRequest request) {
        Product product = Product.builder()
                .name(request.getName())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .active(request.getActive() != null ? request.getActive() : true)
                .isEligibleForCoupon(request.getIsEligibleForCoupon() != null ? request.getIsEligibleForCoupon() : true)
                .build();

        product = productRepository.save(product);
        return mapToResponse(product);
    }

    public ProductResponse getProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));
        return mapToResponse(product);
    }

    public Page<ProductResponse> getAllProducts(int page, int size) {
        return productRepository.findAll(PageRequest.of(page, size))
                .map(product -> mapToResponse(product));
    }

    public Page<ProductResponse> filterProducts(Double minPrice, Double maxPrice, int page, int size) {
        return productRepository.filterByPriceAndActive(minPrice, maxPrice, PageRequest.of(page, size))
                .map(product -> mapToResponse(product));
    }

    public Page<ProductResponse> searchByName(String searchTerm, int page, int size) {
        return productRepository.findByNameContainingIgnoreCase(searchTerm, PageRequest.of(page, size))
                .map(product -> mapToResponse(product));
    }

    public Page<ProductResponse> searchByNameAndPrice(String searchTerm, Double minPrice, Double maxPrice, int page, int size) {
        return productRepository.searchByNameAndPriceRange(searchTerm, minPrice, maxPrice, PageRequest.of(page, size))
                .map(product -> mapToResponse(product));
    }

    public Page<ProductResponse> getActiveProducts(int page, int size) {
        return productRepository.findByActive(true, PageRequest.of(page, size))
                .map(product -> mapToResponse(product));
    }

    public Page<ProductResponse> getCouponEligibleProducts(int page, int size) {
        return productRepository.findByIsEligibleForCoupon(true, PageRequest.of(page, size))
                .map(product -> mapToResponse(product));
    }

    public ProductResponse updateProduct(Long productId, ProductRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));

        if (request.getName() != null) product.setName(request.getName());
        if (request.getPrice() != null) product.setPrice(request.getPrice());
        if (request.getStockQuantity() != null) product.setStockQuantity(request.getStockQuantity());
        if (request.getActive() != null) product.setActive(request.getActive());
        if (request.getIsEligibleForCoupon() != null) product.setIsEligibleForCoupon(request.getIsEligibleForCoupon());

        product = productRepository.save(product);
        return mapToResponse(product);
    }

    public void deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));

        // ==================== MANUAL CASCADE DELETE ====================
        // Delete all dependent records in correct order to avoid constraint violations

        // 1. Delete reviews
        reviewRepository.deleteByProductId(productId);

        // 2. Delete FAQs
        faqRepository.deleteByProductId(productId);

        // 3. Delete product images
        productImageRepository.deleteByProductId(productId);

        // 4. Delete product videos
        productVideoRepository.deleteByProductId(productId);

        // 5. Delete product-coupon links
        productCouponRepository.deleteByProductId(productId);

        // 6. Finally delete the product
        productRepository.delete(product);
    }

    public ProductResponse mapToResponse(Product product) {
        Double avgRating = reviewRepository.getAverageRatingByProductId(product.getId());
        Long reviewCount = reviewRepository.countByProductId(product.getId());

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .active(product.getActive())
                .isEligibleForCoupon(product.getIsEligibleForCoupon())
                .averageRating(avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0)
                .reviewCount(reviewCount)
                .createdAt(product.getCreatedAt())
                .build();
    }
}
