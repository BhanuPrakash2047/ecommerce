package com.snackecommerce.delivery.controller;

import com.snackecommerce.delivery.service.DeliveryService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for handling Delhivery webhook callbacks
 * Delhivery sends updates here when shipment status changes
 */
@RestController
@RequestMapping("/api/delivery/webhook")
public class DeliveryWebhookController {

    private static final Logger logger = LoggerFactory.getLogger(DeliveryWebhookController.class);

    @Autowired
    private DeliveryService deliveryService;

    /**
     * Endpoint to receive Delhivery shipment status updates
     * Called by Delhivery when shipment status changes
     * 
     * Expected payload:
     * {
     *   "waybill": "waybill_number",
     *   "status": "DELIVERED|IN_TRANSIT|PENDING|FAILED",
     *   "location": "current_location",
     *   "timestamp": "2024-01-01T10:00:00"
     * }
     */
    @PostMapping("/shipment-update")
    public ResponseEntity<?> handleShipmentUpdate(@RequestBody String payload) {
        try {
            logger.info("Received Delhivery webhook: {}", payload);

            JSONObject webhookData = new JSONObject(payload);
            String waybill = webhookData.optString("waybill", "");
            String status = webhookData.optString("status", "");
            String location = webhookData.optString("location", "");

            if (waybill.isEmpty()) {
                logger.error("Missing waybill in webhook payload");
                return ResponseEntity.badRequest().body("Missing waybill");
            }

            // Process the delivery update
            deliveryService.handleDeliveryUpdate(waybill, status, location);

            logger.info("Successfully processed shipment update for waybill: {}", waybill);
            return ResponseEntity.ok().body("{\"status\": \"success\"}");
        } catch (Exception e) {
            logger.error("Error processing Delhivery webhook", e);
            return ResponseEntity.badRequest().body("{\"status\": \"error\", \"message\": \"" + e.getMessage() + "\"}");
        }
    }

    /**
     * Health check endpoint for Delhivery to verify webhook is working
     */
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        return ResponseEntity.ok().body("{\"status\": \"healthy\"}");
    }
}
