package com.snackecommerce.payment.controller;

import com.snackecommerce.order.entity.Order;
import com.snackecommerce.order.enums.OrderStatus;
import com.snackecommerce.order.repository.OrderRepository;
import com.snackecommerce.payment.entity.Payment;
import com.snackecommerce.payment.enums.PaymentStatus;
import com.snackecommerce.payment.repository.PaymentRepository;
import com.snackecommerce.payment.service.PaymentService;
import com.snackecommerce.user.entity.User;
import com.snackecommerce.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Admin endpoints for manual payment management
 * Used when webhook fails to reach our server
 */
@RestController
@RequestMapping("/api/admin/payments")
public class AdminPaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * GET /api/admin/payments/pending
     * List all orders in PAYMENT_PENDING status
     * Shows: Order ID, Razorpay Order ID, Amount, Email, Created time, Status
     */
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getPendingPayments() {
        try {
            // Find all orders in PAYMENT_PENDING status
            List<Order> pendingOrders = orderRepository.findByStatus(OrderStatus.PAYMENT_PENDING);
            
            // Enrich with payment details
            List<Map<String, Object>> pendingPayments = pendingOrders.stream().map(order -> {
                Payment payment = paymentRepository.findByOrderId(order.getId()).orElse(null);
                User user = userRepository.findById(order.getUserId()).orElse(null);
                
                Map<String, Object> item = new HashMap<>();
                item.put("orderId", order.getId());
                item.put("orderNumber", order.getOrderNumber());
                item.put("razorpayOrderId", payment != null ? payment.getProviderOrderId() : "N/A");
                item.put("amount", order.getTotalAmountBigDecimal());
                item.put("userEmail", user != null ? user.getEmail() : "N/A");
                item.put("createdAt", order.getCreatedAt());
                item.put("paymentStatus", payment != null ? payment.getStatus() : "UNKNOWN");
                
                return item;
            }).collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("count", pendingPayments.size());
            response.put("payments", pendingPayments);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Failed to fetch pending payments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * POST /api/admin/payments/{orderId}/mark-success
     * Manually mark a payment as successful (webhook failed scenario)
     * 
     * Request: { "verificationNote": "Verified in Razorpay dashboard" }
     * Response: { "status": "success", "message": "Order confirmed and shipment created", "order": {...} }
     */
    @PostMapping("/{orderId}/mark-success")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> markPaymentSuccess(
            @PathVariable Long orderId,
            @RequestBody(required = false) Map<String, String> request) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
            
            if (!order.getStatus().equals(OrderStatus.PAYMENT_PENDING)) {
                Map<String, Object> error = new HashMap<>();
                error.put("status", "error");
                error.put("message", "Order is not in PAYMENT_PENDING status. Current status: " + order.getStatus());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            // Call manual payment success handler
            paymentService.manualMarkPaymentSuccess(orderId);
            
            // Fetch updated order
            Order updatedOrder = orderRepository.findById(orderId).get();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Payment marked SUCCESS, order CONFIRMED, shipment created (or queued for retry)");
            response.put("orderId", updatedOrder.getId());
            response.put("orderNumber", updatedOrder.getOrderNumber());
            response.put("orderStatus", updatedOrder.getStatus());
            response.put("trackingNumber", updatedOrder.getTrackingNumber());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Failed to mark payment as success: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * POST /api/admin/payments/{orderId}/mark-failed
     * Manually mark a payment as failed (webhook failed scenario)
     * 
     * Request: { "reason": "Customer declined payment" }
     * Response: { "status": "success", "message": "Payment marked FAILED, order CANCELLED" }
     */
    @PostMapping("/{orderId}/mark-failed")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> markPaymentFailed(
            @PathVariable Long orderId,
            @RequestBody(required = false) Map<String, String> request) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
            
            if (!order.getStatus().equals(OrderStatus.PAYMENT_PENDING)) {
                Map<String, Object> error = new HashMap<>();
                error.put("status", "error");
                error.put("message", "Order is not in PAYMENT_PENDING status. Current status: " + order.getStatus());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            // Call manual payment failure handler
            paymentService.manualMarkPaymentFailed(orderId);
            
            // Fetch updated order
            Order updatedOrder = orderRepository.findById(orderId).get();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Payment marked FAILED, order CANCELLED");
            response.put("orderId", updatedOrder.getId());
            response.put("orderNumber", updatedOrder.getOrderNumber());
            response.put("orderStatus", updatedOrder.getStatus());
            response.put("reason", request != null ? request.get("reason") : "Admin manual override");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Failed to mark payment as failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * GET /api/admin/payments/stats
     * Get payment statistics
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getPaymentStats() {
        try {
            List<Order> pendingOrders = orderRepository.findByStatus(OrderStatus.PAYMENT_PENDING);
            List<Order> confirmedOrders = orderRepository.findByStatus(OrderStatus.CONFIRMED);
            List<Order> cancelledOrders = orderRepository.findByStatus(OrderStatus.CANCELLED);
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("paymentPending", pendingOrders.size());
            stats.put("confirmed", confirmedOrders.size());
            stats.put("cancelled", cancelledOrders.size());
            stats.put("total", pendingOrders.size() + confirmedOrders.size() + cancelledOrders.size());
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("stats", stats);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Failed to fetch payment stats: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
