package com.snackecommerce.admin.controller;

import com.snackecommerce.admin.service.ShippingLabelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Admin endpoint for downloading shipping labels
 * Allows admin to download single or batch shipping labels from Delhivery
 */
@RestController
@RequestMapping("/api/admin/shipping-labels")
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
public class AdminShippingLabelController {

    @Autowired
    private ShippingLabelService shippingLabelService;

    /**
     * GET /api/admin/shipping-labels/{orderId}/download
     * Download shipping label for a single order as PDF
     * 
     * @param orderId Order ID
     * @return PDF file of the shipping label
     */
    @GetMapping("/{orderId}/download")
    public ResponseEntity<byte[]> downloadOrderLabel(@PathVariable Long orderId) {
        try {
            log.info("Admin downloading shipping label for order ID: {}", orderId);
            
            byte[] labelPdf = shippingLabelService.downloadOrderLabel(orderId);
            
            if (labelPdf == null || labelPdf.length == 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(null);
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"label_order_" + orderId + ".pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(labelPdf);
                    
        } catch (IllegalArgumentException e) {
            log.warn("Invalid order or missing tracking: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Error downloading label for order {}: {}", orderId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/admin/shipping-labels/batch/download
     * Download all shipping labels as a ZIP file
     * 
     * @param orderIds Optional comma-separated order IDs (if not provided, downloads all shipped orders)
     * @param status Optional order status filter (default: SHIPPED)
     * @return ZIP file containing all labels
     */
    @GetMapping("/batch/download")
    public ResponseEntity<byte[]> downloadBatchLabels(
            @RequestParam(required = false) String orderIds,
            @RequestParam(required = false, defaultValue = "SHIPPED") String status) {
        try {
            log.info("Admin downloading batch shipping labels - orderIds: {}, status: {}", orderIds, status);
            
            List<Long> requestedOrderIds = null;
            if (orderIds != null && !orderIds.isEmpty()) {
                String[] ids = orderIds.split(",");
                requestedOrderIds = new java.util.ArrayList<>();
                for (String id : ids) {
                    try {
                        requestedOrderIds.add(Long.parseLong(id.trim()));
                    } catch (NumberFormatException e) {
                        log.warn("Invalid order ID format: {}", id);
                    }
                }
            }

            byte[] zipFile = shippingLabelService.downloadBatchLabels(requestedOrderIds, status);
            
            if (zipFile == null || zipFile.length == 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(null);
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"shipping_labels_" + System.currentTimeMillis() + ".zip\"")
                    .contentType(MediaType.parseMediaType("application/zip"))
                    .body(zipFile);
                    
        } catch (Exception e) {
            log.error("Error downloading batch labels: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/admin/shipping-labels/batch/preview
     * Preview which orders will be included in batch download
     * 
     * @param orderIds Optional comma-separated order IDs
     * @param status Optional order status filter
     * @return JSON with list of orders that have labels available
     */
    @GetMapping("/batch/preview")
    public ResponseEntity<Map<String, Object>> previewBatchLabels(
            @RequestParam(required = false) String orderIds,
            @RequestParam(required = false, defaultValue = "SHIPPED") String status) {
        try {
            log.info("Admin previewing batch shipping labels - status: {}", status);
            
            List<Long> requestedOrderIds = null;
            if (orderIds != null && !orderIds.isEmpty()) {
                String[] ids = orderIds.split(",");
                requestedOrderIds = new java.util.ArrayList<>();
                for (String id : ids) {
                    try {
                        requestedOrderIds.add(Long.parseLong(id.trim()));
                    } catch (NumberFormatException e) {
                        log.warn("Invalid order ID format: {}", id);
                    }
                }
            }

            var orderDetails = shippingLabelService.getOrdersForBatchDownload(requestedOrderIds, status);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("totalOrders", orderDetails.size());
            response.put("orders", orderDetails);
            
            return ResponseEntity.ok(response);
                    
        } catch (Exception e) {
            log.error("Error previewing batch labels: {}", e.getMessage(), e);
            
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * GET /api/admin/shipping-labels/generate-all
     * Regenerate all missing labels for shipped orders
     * This is a utility endpoint to bulk recreate labels if some are missing
     * 
     * @return Summary of regeneration results
     */
    @PostMapping("/regenerate-all")
    public ResponseEntity<Map<String, Object>> regenerateAllLabels() {
        try {
            log.info("Admin regenerating all shipping labels for shipped orders");
            
            var result = shippingLabelService.regenerateAllMissingLabels();

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Label regeneration completed");
            response.put("results", result);
            
            return ResponseEntity.ok(response);
                    
        } catch (Exception e) {
            log.error("Error regenerating labels: {}", e.getMessage(), e);
            
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
