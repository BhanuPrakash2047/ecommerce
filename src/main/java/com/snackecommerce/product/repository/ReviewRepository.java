package com.snackecommerce.product.repository;

import com.snackecommerce.product.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findByProductId(Long productId, Pageable pageable);
    
    List<Review> findByProductIdOrderByCreatedAtDesc(Long productId);
    
    Optional<Review> findByProductIdAndUserId(Long productId, Long userId);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = ?1")
    Double getAverageRatingByProductId(Long productId);
    
    Long countByProductId(Long productId);

    // Delete by product (manual cascade)
    void deleteByProductId(Long productId);
}
