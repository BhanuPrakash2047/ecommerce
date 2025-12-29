package com.snackecommerce.cart.repository;

import com.snackecommerce.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // Find all items in a cart
    List<CartItem> findByCartId(Long cartId);

    // Find specific item by cart and product (prevent duplicates)
    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);

    // Delete all items from a cart
    void deleteByCartId(Long cartId);

    // Count items in cart
    Long countByCartId(Long cartId);

    // Get items for a specific product across all carts (for stock checks)
    @Query("""
            SELECT ci FROM CartItem ci 
            WHERE ci.productId = :productId 
            AND ci.cartId IN (
                SELECT c.id FROM Cart c WHERE c.status = 'ACTIVE'
            )
            """)
    List<CartItem> findActiveItemsByProductId(@Param("productId") Long productId);

    // Total quantity of product in active carts
    @Query("""
            SELECT COALESCE(SUM(ci.quantity), 0) FROM CartItem ci 
            WHERE ci.productId = :productId 
            AND ci.cartId IN (
                SELECT c.id FROM Cart c WHERE c.status = 'ACTIVE'
            )
            """)
    Integer getTotalQuantityInActiveCarts(@Param("productId") Long productId);
}
