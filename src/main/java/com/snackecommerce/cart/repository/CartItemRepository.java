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


}
