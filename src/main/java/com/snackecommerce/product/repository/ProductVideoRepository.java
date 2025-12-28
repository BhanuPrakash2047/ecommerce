package com.snackecommerce.product.repository;

import com.snackecommerce.product.entity.ProductVideo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductVideoRepository extends JpaRepository<ProductVideo, Long> {
    List<ProductVideo> findByProductIdOrderByDisplayOrder(Long productId);
    
    @Query("SELECT pv FROM ProductVideo pv WHERE pv.product.id = ?1 ORDER BY pv.displayOrder ASC")
    List<ProductVideo> getVideosForProduct(Long productId);

    // Delete by product (manual cascade)
    void deleteByProductId(Long productId);
}
