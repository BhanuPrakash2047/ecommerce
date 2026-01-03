package com.snackecommerce.notification.service;

import com.snackecommerce.notification.entity.Notification;
import com.snackecommerce.notification.enums.NotificationType;
import com.snackecommerce.notification.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private NotificationRepository notificationRepository;

    /**
     * Send notification: Payment received, order processing
     * Sent on: Payment success (immediate or admin manual approval)
     */
    public Notification notifyPaymentReceived(Long userId, Long orderId, String orderNumber, Double amount) {
        String title = "Payment Received ✅";
        String message = String.format("Your payment of ₹%.2f for order %s has been received. We're now processing your order.", amount, orderNumber);
        
        Notification notification = createNotification(
            userId,
            title,
            message,
            NotificationType.PAYMENT_RECEIVED,
            "ORDER",
            orderId,
            null
        );
        
        logger.info("Notification sent to user {} for payment received (order {})", userId, orderId);
        return notification;
    }

    /**
     * Send notification: Shipment created with tracking
     * Sent on: Shipment created successfully (immediate or after retries)
     */
    public Notification notifyShipmentCreated(Long userId, Long orderId, String orderNumber, String trackingNumber, String shippingLabelUrl) {
        String title = "Shipment Created 📦";
        String message = String.format("Your order %s has been shipped! Tracking number: %s", orderNumber, trackingNumber);
        
        String metadata = String.format("{\"trackingNumber\":\"%s\",\"labelUrl\":\"%s\"}", trackingNumber, shippingLabelUrl);
        
        Notification notification = createNotification(
            userId,
            title,
            message,
            NotificationType.SHIPMENT_CREATED,
            "ORDER",
            orderId,
            metadata
        );
        
        logger.info("Notification sent to user {} for shipment created (order {}, tracking: {})", userId, orderId, trackingNumber);
        return notification;
    }

    /**
     * Send notification: Order delivered
     * Sent on: Delhivery webhook delivery update
     */
    public Notification notifyOrderDelivered(Long userId, Long orderId, String orderNumber, LocalDateTime deliveryTime) {
        String title = "Order Delivered 🎉";
        String message = String.format("Your order %s has been successfully delivered at %s. Thank you for shopping with us!", orderNumber, deliveryTime);
        
        String metadata = String.format("{\"deliveryTime\":\"%s\"}", deliveryTime);
        
        Notification notification = createNotification(
            userId,
            title,
            message,
            NotificationType.ORDER_DELIVERED,
            "ORDER",
            orderId,
            metadata
        );
        
        logger.info("Notification sent to user {} for order delivered (order {})", userId, orderId);
        return notification;
    }

    /**
     * Send notification to ADMIN: Shipment job failed after max retries
     * Sent on: ShipmentJobService marks job as FAILED
     */
    public Notification notifyAdminShipmentFailure(Long adminUserId, Long orderId, String orderNumber, int attempts, String lastError) {
        String title = "⚠️ ADMIN ALERT: Shipment Creation Failed";
        String message = String.format("Shipment creation failed for order %s after %d attempts. Last error: %s. Manual intervention required.", orderNumber, attempts, lastError);
        
        String metadata = String.format("{\"attempts\":%d,\"lastError\":\"%s\"}", attempts, lastError);
        
        Notification notification = createNotification(
            adminUserId,
            title,
            message,
            NotificationType.ADMIN_SHIPMENT_FAILED,
            "SHIPMENT_JOB",
            orderId,
            metadata
        );
        
        logger.info("Admin notification sent for failed shipment (order {}, admin user {})", orderId, adminUserId);
        return notification;
    }

    /**
     * Generic notification creation method
     */
    private Notification createNotification(Long userId, String title, String message, NotificationType type, 
                                           String relatedEntityType, Long relatedEntityId, String metadata) {
        Notification notification = Notification.builder()
                .userId(userId)
                .title(title)
                .message(message)
                .type(type)
                .relatedEntityType(relatedEntityType)
                .relatedEntityId(relatedEntityId)
                .metadata(metadata)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        
        return notificationRepository.save(notification);
    }

    /**
     * Get all unread notifications for a user
     */
    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
    }

    /**
     * Get all notifications for a user (recent first)
     */
    public List<Notification> getNotifications(Long userId, int limit) {
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return notifications.size() > limit ? notifications.subList(0, limit) : notifications;
    }

    /**
     * Count unread notifications for a user
     */
    public Long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    /**
     * Mark a notification as read
     */
    public Notification markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));
        
        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now());
        
        return notificationRepository.save(notification);
    }

    /**
     * Mark all unread notifications as read
     */
    public void markAllAsRead(Long userId) {
        List<Notification> unreadNotifications = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        LocalDateTime now = LocalDateTime.now();
        
        for (Notification notification : unreadNotifications) {
            notification.setIsRead(true);
            notification.setReadAt(now);
        }
        
        notificationRepository.saveAll(unreadNotifications);
        logger.info("Marked {} notifications as read for user {}", unreadNotifications.size(), userId);
    }

    /**
     * Delete a notification
     */
    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
        logger.info("Notification deleted: {}", notificationId);
    }

    /**
     * Delete all notifications for a user
     */
    public void deleteAllNotifications(Long userId) {
        List<Notification> userNotifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        notificationRepository.deleteAll(userNotifications);
        logger.info("Deleted {} notifications for user {}", userNotifications.size(), userId);
    }
}
