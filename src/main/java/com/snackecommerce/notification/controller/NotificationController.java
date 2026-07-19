package com.snackecommerce.notification.controller;

import com.snackecommerce.notification.entity.Notification;
import com.snackecommerce.notification.service.NotificationService;
import com.snackecommerce.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User endpoints for managing in-app notifications
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserRepository userRepository;

    /**
     * GET /api/notifications/unread
     * Get all unread notifications for current user
     */
    @GetMapping("/unread")
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getUnreadNotifications() {
        try {
            Long userId = getCurrentUserId();
            List<Notification> unreadNotifications = notificationService.getUnreadNotifications(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("count", unreadNotifications.size());
            response.put("notifications", unreadNotifications);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Failed to fetch unread notifications: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * GET /api/notifications
     * Get all notifications for current user (with optional limit)
     * Query param: limit (default 20)
     */
    @GetMapping
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getNotifications(
            @RequestParam(value = "limit", defaultValue = "20") int limit) {
        try {
            Long userId = getCurrentUserId();
            List<Notification> notifications = notificationService.getNotifications(userId, limit);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("count", notifications.size());
            response.put("notifications", notifications);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Failed to fetch notifications: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * GET /api/notifications/unread-count
     * Get count of unread notifications
     */
    @GetMapping("/unread-count")
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getUnreadCount() {
        try {
            Long userId = getCurrentUserId();
            Long unreadCount = notificationService.getUnreadCount(userId);
            
            Map<String, Object> response = new HashMap<>();
             response.put("status", "success");
            response.put("unreadCount", unreadCount);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Failed to get unread count: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * PUT /api/notifications/{notificationId}/read
     * Mark a specific notification as read
     */
    @PutMapping("/{notificationId}/read")
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> markAsRead(@PathVariable Long notificationId) {
        try {
            Long userId = getCurrentUserId();
            Notification notification = notificationService.markAsRead(notificationId, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Notification marked as read");
            response.put("notification", notification);
            
            return ResponseEntity.ok(response);
        } catch (AccessDeniedException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Failed to mark notification as read: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).body(error);
        }
    }

    /**
     * PUT /api/notifications/read-all
     * Mark all unread notifications as read for current user
     */
    @PutMapping("/read-all")
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> markAllAsRead() {
        try {
            Long userId = getCurrentUserId();
            notificationService.markAllAsRead(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "All notifications marked as read");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Failed to mark all notifications as read: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * DELETE /api/notifications/{notificationId}
     * Delete a specific notification
     */
    @DeleteMapping("/{notificationId}")
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteNotification(@PathVariable Long notificationId) {
        try {
            Long userId = getCurrentUserId();
            notificationService.deleteNotification(notificationId, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Notification deleted");
            
            return ResponseEntity.ok(response);
        } catch (AccessDeniedException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Failed to delete notification: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * DELETE /api/notifications/all
     * Delete all notifications for current user
     */
    @DeleteMapping("/all")
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteAllNotifications() {
        try {
            Long userId = getCurrentUserId();
            notificationService.deleteAllNotifications(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "All notifications deleted");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Failed to delete all notifications: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Helper method to get current user ID from security context
     */
    private Long getCurrentUserId() {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email))
                .getId();
    }
}
