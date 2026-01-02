package com.snackecommerce.product.service;

import com.snackecommerce.common.exception.ProductNotFoundException;
import com.snackecommerce.product.dto.ProductRequest;
import com.snackecommerce.product.dto.ProductResponse;
import com.snackecommerce.product.entity.Product;
import com.snackecommerce.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Simplified Product Service
 * Handles CRUD operations for products with simplified schema
 */
@Service
@Transactional
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    @Autowired
    private ProductRepository productRepository;

    public ProductResponse createProduct(ProductRequest request) {
        Product product = Product.builder()
                .name(request.getName())
                .price(request.getPrice())
                .originalPrice(request.getOriginalPrice())
                .isAvailable(true)
                .isEligibleForCoupon(request.getIsEligibleForCoupon() != null ? request.getIsEligibleForCoupon() : true)
                .createdAt(LocalDateTime.now())
                .build();

        product = productRepository.save(product);
        logger.info("Product created: {} with price: ₹{}", product.getName(), product.getPrice());
        return mapToResponse(product);
    }

    public ProductResponse updateProduct(Long productId, ProductRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));

        if (request.getName() != null) {
            product.setName(request.getName());
        }

        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }

        if (request.getOriginalPrice() != null) {
            product.setOriginalPrice(request.getOriginalPrice());
        }

        if (request.getIsAvailable() != null) {
            product.setIsAvailable(request.getIsAvailable());
        }

        if (request.getIsEligibleForCoupon() != null) {
            product.setIsEligibleForCoupon(request.getIsEligibleForCoupon());
        }

        product = productRepository.save(product);
        logger.info("Product updated: {}", product.getName());
        return mapToResponse(product);
    }

    public void deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));
        productRepository.delete(product);
        logger.info("Product deleted: {}", product.getName());
    }

    public ProductResponse getProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));
        return mapToResponse(product);
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ProductResponse> getAvailableProducts() {
        return productRepository.findByIsAvailableTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ProductResponse> getCouponEligibleProducts() {
        return productRepository.findByIsEligibleForCouponTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Stub methods for complex filtering (not implemented in simplified architecture)
    public List<ProductResponse> filterProducts(Double minPrice, Double maxPrice, int page, int size) {
        return productRepository.findAll().stream()
                .filter(p -> p.getPrice() >= minPrice && p.getPrice() <= maxPrice)
                .skip((long) page * size)
                .limit(size)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ProductResponse> searchByName(String name, int page, int size) {
        return productRepository.findAll().stream()
                .filter(p -> p.getName().toLowerCase().contains(name.toLowerCase()))
                .skip((long) page * size)
                .limit(size)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ProductResponse> searchByNameAndPrice(String name, Double minPrice, Double maxPrice, int page, int size) {
        return productRepository.findAll().stream()
                .filter(p -> p.getName().toLowerCase().contains(name.toLowerCase()))
                .filter(p -> p.getPrice() >= minPrice && p.getPrice() <= maxPrice)
                .skip((long) page * size)
                .limit(size)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ==================== HELPER METHODS ====================

    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .originalPrice(product.getOriginalPrice())
                .isAvailable(product.getIsAvailable())
                .isEligibleForCoupon(product.getIsEligibleForCoupon())
                .createdAt(product.getCreatedAt())
                .build();
    }
}
