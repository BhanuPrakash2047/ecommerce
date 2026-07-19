package com.snackecommerce.order.controller;

import com.snackecommerce.common.annotation.RateLimit;
import com.snackecommerce.delivery.dto.PincodeAvailabilityResponse;
import com.snackecommerce.delivery.dto.TrackingResponse;
import com.snackecommerce.delivery.service.DeliveryService;
import com.snackecommerce.order.dto.AdminOrderStatsRequest;
import com.snackecommerce.order.dto.AdminOrderStatsResponse;
import com.snackecommerce.order.dto.OrderListResponse;
import com.snackecommerce.order.dto.OrderResponse;
import com.snackecommerce.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

/**
 * Controller for order tracking and user orders
 */
@RestController
@RequestMapping("/api/orders")
public class OrderTrackingController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private DeliveryService deliveryService;

    /**
     * Get all orders for logged-in user
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @RateLimit(value = "orders", useAuthenticatedUser = true)
    public ResponseEntity<List<OrderListResponse>> getUserOrders(Principal principal) {
        List<OrderListResponse> orders = orderService.getUserOrdersByEmail(principal.getName());
        return ResponseEntity.ok(orders);
    }

    /**
     * Get specific order details for user
     */
    @GetMapping("/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrderResponse> getOrderDetails(@PathVariable Long orderId, Principal principal) {
        OrderResponse order = orderService.getOrderDetails(orderId, principal.getName());
        return ResponseEntity.ok(order);
    }

    /**
     * Track order delivery status
     */
    @GetMapping("/{orderId}/track")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TrackingResponse> trackOrder(@PathVariable Long orderId, Principal principal) {
        try {
            TrackingResponse tracking = orderService.trackOrder(orderId, principal.getName());
            return ResponseEntity.ok(tracking);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403).build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Download shipping label for an order
     * Downloads on-the-fly from Delhivery API
     */
    @GetMapping("/{orderId}/shipping-label")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> downloadShippingLabel(@PathVariable Long orderId, Principal principal) {
        try {
            byte[] labelData = deliveryService.downloadShippingLabel(orderId);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=shipping_label_" + orderId + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(labelData);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Check if pincode is serviceable for delivery
     */
    @GetMapping("/delivery/check-pincode/{pincode}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PincodeAvailabilityResponse> checkPincodeAvailability(@PathVariable String pincode) {
        try {
            PincodeAvailabilityResponse response = deliveryService.checkPincodeAvailability(pincode);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Admin endpoint: Get all orders with tracking
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderListResponse>> getAllOrders() {
        List<OrderListResponse> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    /**
     * Admin endpoint: Get comprehensive order statistics with filters
     * 
     * Single endpoint for all admin order stats and filtering needs.
     * 
     * Query params:
     * - startDate: Filter orders from this date (yyyy-MM-dd)
     * - endDate: Filter orders until this date (yyyy-MM-dd)
     * - status: Filter by order status (CREATED, PAYMENT_PENDING, PAID, CONFIRMED, SHIPPED, DELIVERED, RETURNED, CANCELLED)
     * - includeRecentOrders: Include recent orders list (default: true)
     * - includeTopProducts: Include top selling products (default: false)
     * - recentOrdersLimit: Number of recent orders to include (default: 10)
     * - includeAllOrders: Include all filtered orders in response (default: false)
     * 
     * Example usage:
     * - GET /admin/stats - Get all stats
     * - GET /admin/stats?status=DELIVERED - Stats for delivered orders only
     * - GET /admin/stats?startDate=2026-01-01&endDate=2026-01-31 - Stats for January
     * - GET /admin/stats?includeAllOrders=true&status=PENDING - Get all pending orders with stats
     * - GET /admin/stats?includeTopProducts=true - Include top selling products
     */
    @GetMapping("/admin/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminOrderStatsResponse> getAdminOrderStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "true") Boolean includeRecentOrders,
            @RequestParam(required = false, defaultValue = "false") Boolean includeTopProducts,
            @RequestParam(required = false, defaultValue = "10") Integer recentOrdersLimit,
            @RequestParam(required = false, defaultValue = "false") Boolean includeAllOrders) {
        
        AdminOrderStatsRequest request = AdminOrderStatsRequest.builder()
                .startDate(startDate)
                .endDate(endDate)
                .status(status)
                .includeRecentOrders(includeRecentOrders)
                .includeTopProducts(includeTopProducts)
                .recentOrdersLimit(recentOrdersLimit)
                .includeAllOrders(includeAllOrders)
                .build();
        
        AdminOrderStatsResponse stats = orderService.getAdminOrderStats(request);
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Admin endpoint: Get specific order details (admin can view any order)
     */
    @GetMapping("/admin/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> getOrderDetailsAdmin(@PathVariable Long orderId) {
        OrderResponse order = orderService.getOrderDetails(orderId, null);
        return ResponseEntity.ok(order);
    }
}
