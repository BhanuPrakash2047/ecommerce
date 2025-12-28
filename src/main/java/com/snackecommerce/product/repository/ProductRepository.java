package com.snackecommerce.product.repository;

import com.snackecommerce.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Find products by price range
    Page<Product> findByPriceBetween(Double minPrice, Double maxPrice, Pageable pageable);

    // Find products by name (case-insensitive)
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // Find products by active status
    Page<Product> findByActive(Boolean active, Pageable pageable);

    // Find products eligible for coupons
    Page<Product> findByIsEligibleForCoupon(Boolean isEligible, Pageable pageable);

    // Combined filter: price range and active status
    @Query("""
            SELECT p FROM Product p
            WHERE p.price BETWEEN :minPrice AND :maxPrice
            AND p.active = true
            ORDER BY p.price ASC
            """)
    Page<Product> filterByPriceAndActive(
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            Pageable pageable
    );

    // Search by name with price range
    @Query("""
            SELECT p FROM Product p
            WHERE p.name LIKE %:searchTerm%
            AND p.price BETWEEN :minPrice AND :maxPrice
            AND p.active = true
            ORDER BY p.name ASC
            """)
    Page<Product> searchByNameAndPriceRange(
            @Param("searchTerm") String searchTerm,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            Pageable pageable
    );

    // Find low stock products (for admin alerts)
    @Query("""
            SELECT p FROM Product p
            WHERE p.stockQuantity < :threshold
            AND p.active = true
            ORDER BY p.stockQuantity ASC
            """)
    List<Product> findLowStockProducts(@Param("threshold") Integer threshold);

    // Count products by active status
    Long countByActive(Boolean active);
}
