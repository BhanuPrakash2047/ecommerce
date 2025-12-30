package com.snackecommerce.order.repository;

import com.snackecommerce.order.entity.Order;
import com.snackecommerce.order.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    /**
     * Find all orders for a user with specific status
     */
    List<Order> findByUserIdAndStatus(Long userId, OrderStatus status);
    
    /**
     * Find all orders for a user
     */
    List<Order> findByUserId(Long userId);
    
    /**
     * Find all orders with specific status
     */
    List<Order> findByStatus(OrderStatus status);
}
