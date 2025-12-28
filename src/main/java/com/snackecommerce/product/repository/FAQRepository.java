package com.snackecommerce.product.repository;

import com.snackecommerce.product.entity.FAQ;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FAQRepository extends JpaRepository<FAQ, Long> {
    List<FAQ> findByProductIdOrderByDisplayOrder(Long productId);
    
    @Query("SELECT f FROM FAQ f WHERE f.product.id = ?1 ORDER BY f.displayOrder ASC")
    List<FAQ> getFAQsForProduct(Long productId);

    // Delete by product (manual cascade)
    void deleteByProductId(Long productId);
}
