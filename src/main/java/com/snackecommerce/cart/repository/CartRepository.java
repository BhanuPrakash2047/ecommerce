package com.snackecommerce.cart.repository;

import com.snackecommerce.cart.entity.Cart;
import com.snackecommerce.cart.enums.CartStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    // Find active cart for user
    Optional<Cart> findByUserIdAndStatus(Long userId, CartStatus status);

    // Find any cart for user (for cleanup/merge)
    Optional<Cart> findByUserId(Long userId);

    // Pessimistic locking for checkout (prevents concurrent checkout)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Cart c WHERE c.id = :cartId")
    Optional<Cart> findByIdForCheckout(@Param("cartId") Long cartId);

    // Find abandoned carts (not updated for 30+ days)
    @Query("""
            SELECT c FROM Cart c 
            WHERE c.updatedAt < :thirtyDaysAgo 
            AND c.status = 'ACTIVE'
            """)
    List<Cart> findAbandonedCarts(@Param("thirtyDaysAgo") LocalDateTime thirtyDaysAgo);

    // Count user's active carts
    Long countByUserIdAndStatus(Long userId, CartStatus status);
}
