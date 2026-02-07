package com.snackecommerce.order.repository;

import com.snackecommerce.order.entity.Order;
import com.snackecommerce.order.enums.OrderStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
    
    /**
     * Find order by tracking number (waybill)
     */
    Optional<Order> findByTrackingNumber(String trackingNumber);
    
    // ==================== ADMIN STATS QUERIES ====================
    
    /**
     * Find orders created between dates
     */
    List<Order> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find orders by status created between dates
     */
    List<Order> findByStatusAndCreatedAtBetween(OrderStatus status, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find orders created after a specific date
     */
    List<Order> findByCreatedAtAfter(LocalDateTime startDate);
    
    /**
     * Find orders delivered between dates
     */
    List<Order> findByDeliveredAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find orders delivered after a specific date
     */
    List<Order> findByDeliveredAtAfter(LocalDateTime startDate);
    
    /**
     * Count orders by status
     */
    Long countByStatus(OrderStatus status);
    
    /**
     * Count orders created between dates
     */
    Long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Count orders by status created between dates
     */
    Long countByStatusAndCreatedAtBetween(OrderStatus status, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Get recent orders ordered by created date descending
     */
    List<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    /**
     * Get recent orders by date range ordered by created date descending
     */
    List<Order> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    /**
     * Sum total amount for orders with specific status
     */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = :status")
    Double sumTotalAmountByStatus(@Param("status") OrderStatus status);
    
    /**
     * Sum total amount for orders created between dates
     */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    Double sumTotalAmountByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    /**
     * Sum total amount for orders with specific status created between dates
     */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = :status AND o.createdAt BETWEEN :startDate AND :endDate")
    Double sumTotalAmountByStatusAndCreatedAtBetween(@Param("status") OrderStatus status, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
