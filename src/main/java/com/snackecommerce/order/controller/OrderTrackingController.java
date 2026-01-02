package com.snackecommerce.order.controller;

import com.snackecommerce.delivery.dto.PincodeAvailabilityResponse;
import com.snackecommerce.delivery.dto.TrackingResponse;
import com.snackecommerce.delivery.service.DeliveryService;
import com.snackecommerce.order.dto.OrderListResponse;
import com.snackecommerce.order.dto.OrderResponse;
import com.snackecommerce.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
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
    public ResponseEntity<TrackingResponse> trackOrder(@PathVariable Long orderId) {
        try {
            TrackingResponse tracking = deliveryService.trackOrder(orderId);
            return ResponseEntity.ok(tracking);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Download shipping label for an order
     * Downloads on-the-fly from Delhivery API
     */
    @GetMapping("/{orderId}/shipping-label")
    @PreAuthorize("isAuthenticated()")
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
     * Admin endpoint: Get order analytics
     */
    @GetMapping("/admin/analytics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getOrderAnalytics() {
        try {
            return ResponseEntity.ok(orderService.getOrderAnalytics());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching analytics");
        }
    }
}
