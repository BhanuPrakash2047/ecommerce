package com.snackecommerce.notification.repository;

import com.snackecommerce.notification.entity.Notification;
import com.snackecommerce.notification.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Find all notifications for a user, ordered by most recent first
     */
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Find unread notifications for a user
     */
    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);

    /**
     * Count unread notifications for a user
     */
    Long countByUserIdAndIsReadFalse(Long userId);

    /**
     * Find notifications by type and related entity (e.g., all ORDER_DELIVERED for orderId=5)
     */
    @Query("SELECT n FROM Notification n WHERE n.type = :type AND n.relatedEntityId = :entityId")
    List<Notification> findByTypeAndEntity(@Param("type") NotificationType type, @Param("entityId") Long entityId);

    /**
     * Check if notification already exists for this event (to avoid duplicates)
     */
    @Query("SELECT COUNT(n) > 0 FROM Notification n WHERE n.userId = :userId AND n.type = :type AND n.relatedEntityId = :entityId")
    boolean existsByUserAndTypeAndEntity(@Param("userId") Long userId, @Param("type") NotificationType type, @Param("entityId") Long entityId);
}
