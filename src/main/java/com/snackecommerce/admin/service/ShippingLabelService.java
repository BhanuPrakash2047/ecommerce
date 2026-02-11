package com.snackecommerce.admin.service;

import com.snackecommerce.delivery.util.DelhiveryUtil;
import com.snackecommerce.order.entity.Order;
import com.snackecommerce.order.enums.OrderStatus;
import com.snackecommerce.order.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Service for managing shipping label downloads
 * Handles single order and batch downloads from Delhivery API
 */
@Service
@Transactional
@Slf4j
public class ShippingLabelService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private DelhiveryUtil delhiveryUtil;

    /**
     * Download shipping label for a single order
     * 
     * @param orderId Order ID
     * @return PDF bytes of the shipping label
     * @throws IllegalArgumentException if order not found or has no tracking number
     * @throws Exception if Delhivery API call fails
     */
    public byte[] downloadOrderLabel(Long orderId) throws Exception {
        log.info("Downloading label for order ID: {}", orderId);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        
        if (order.getTrackingNumber() == null || order.getTrackingNumber().isEmpty()) {
            throw new IllegalArgumentException("Order " + orderId + " has no tracking number. Order status: " + order.getStatus());
        }
        
        log.info("Downloading label for waybill: {}", order.getTrackingNumber());
        
        try {
            byte[] labelPdf = delhiveryUtil.downloadShippingLabel(order.getTrackingNumber());
            
            if (labelPdf == null || labelPdf.length == 0) {
                log.warn("Downloaded label is empty for waybill: {}", order.getTrackingNumber());
            }
            
            return labelPdf;
        } catch (Exception e) {
            log.error("Failed to download label for order {}, waybill {}: {}", 
                    orderId, order.getTrackingNumber(), e.getMessage(), e);
            throw new RuntimeException("Failed to download shipping label: " + e.getMessage(), e);
        }
    }

    /**
     * Download multiple shipping labels as a ZIP archive
     * 
     * @param orderIds List of order IDs (if null, gets all shipped orders)
     * @param status Order status to filter by (default: SHIPPED)
     * @return ZIP file contents as byte array
     * @throws Exception if download fails
     */
    public byte[] downloadBatchLabels(List<Long> orderIds, String status) throws Exception {
        log.info("Downloading batch labels - orderIds: {}, status: {}", orderIds, status);
        
        List<Order> orders = getOrdersForBatch(orderIds, status);
        
        if (orders.isEmpty()) {
            throw new IllegalArgumentException("No orders found matching criteria");
        }
        
        log.info("Downloading labels for {} orders", orders.size());
        
        try {
            return createZipArchive(orders);
        } catch (Exception e) {
            log.error("Failed to create ZIP archive: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create ZIP archive: " + e.getMessage(), e);
        }
    }

    /**
     * Get list of orders for batch processing
     * 
     * @param orderIds Specific order IDs (if null, gets all by status)
     * @param status Order status to filter by
     * @return List of orders with tracking information
     */
    public List<Order> getOrdersForBatch(List<Long> orderIds, String status) {
        List<Order> orders = new ArrayList<>();
        
        try {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            
            if (orderIds != null && !orderIds.isEmpty()) {
                // Get specific orders
                for (Long orderId : orderIds) {
                    var order = orderRepository.findById(orderId);
                    if (order.isPresent() && order.get().getTrackingNumber() != null) {
                        orders.add(order.get());
                    }
                }
                log.info("Found {} orders from requested IDs with tracking", orders.size());
            } else {
                // Get all orders with given status
                orders = orderRepository.findByStatus(orderStatus);
                // Filter only those with tracking numbers
                orders = orders.stream()
                        .filter(o -> o.getTrackingNumber() != null && !o.getTrackingNumber().isEmpty())
                        .toList();
                log.info("Found {} {} orders with tracking numbers", orders.size(), status);
            }
        } catch (IllegalArgumentException e) {
            log.warn("Invalid order status: {}", status);
            // Fall back to SHIPPED
            var shippedOrders = orderRepository.findByStatus(OrderStatus.SHIPPED);
            orders = shippedOrders.stream()
                    .filter(o -> o.getTrackingNumber() != null && !o.getTrackingNumber().isEmpty())
                    .toList();
        }
        
        return orders;
    }

    /**
     * Get list of order details for preview
     * 
     * @param orderIds Specific order IDs (if null, gets all by status)
     * @param status Order status to filter by
     * @return List of orders with minimal details
     */
    public List<Map<String, Object>> getOrdersForBatchDownload(List<Long> orderIds, String status) {
        List<Order> orders = getOrdersForBatch(orderIds, status);
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Order order : orders) {
            Map<String, Object> orderInfo = new HashMap<>();
            orderInfo.put("id", order.getId());
            orderInfo.put("orderNumber", order.getOrderNumber());
            orderInfo.put("trackingNumber", order.getTrackingNumber());
            orderInfo.put("receiverName", order.getReceiverName());
            orderInfo.put("status", order.getStatus().toString());
            orderInfo.put("createdAt", order.getCreatedAt());
            result.add(orderInfo);
        }
        
        return result;
    }

    /**
     * Create a ZIP archive containing all shipping labels
     * 
     * @param orders List of orders to download labels for
     * @return ZIP file as byte array
     * @throws IOException if ZIP creation fails
     */
    private byte[] createZipArchive(List<Order> orders) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);
        
        int successCount = 0;
        int failureCount = 0;
        List<String> failedOrders = new ArrayList<>();
        
        for (Order order : orders) {
            try {
                log.info("Adding label for order {} (waybill: {}) to ZIP", 
                        order.getOrderNumber(), order.getTrackingNumber());
                
                byte[] labelPdf = delhiveryUtil.downloadShippingLabel(order.getTrackingNumber());
                
                if (labelPdf != null && labelPdf.length > 0) {
                    // Create filename: ORDER_NUMBER_WAYBILL_TIMESTAMP.pdf
                    String filename = String.format("%s_%s_%s.pdf",
                            order.getOrderNumber(),
                            order.getTrackingNumber(),
                            System.currentTimeMillis());
                    
                    ZipEntry zipEntry = new ZipEntry(filename);
                    zipOutputStream.putNextEntry(zipEntry);
                    zipOutputStream.write(labelPdf);
                    zipOutputStream.closeEntry();
                    
                    successCount++;
                    log.debug("Added {} to ZIP (size: {} bytes)", filename, labelPdf.length);
                } else {
                    failureCount++;
                    failedOrders.add(order.getOrderNumber());
                    log.warn("Label PDF is empty for order {}", order.getOrderNumber());
                }
            } catch (Exception e) {
                failureCount++;
                failedOrders.add(order.getOrderNumber());
                log.warn("Failed to download label for order {}: {}", order.getOrderNumber(), e.getMessage());
            }
        }
        
        // Add summary file to ZIP
        String summaryContent = String.format(
                "Shipping Label Batch Download Summary\n" +
                "======================================\n" +
                "Generated: %s\n" +
                "Total Orders: %d\n" +
                "Successful: %d\n" +
                "Failed: %d\n" +
                "%s",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                orders.size(),
                successCount,
                failureCount,
                failureCount > 0 ? "\nFailed Orders:\n" + String.join("\n", failedOrders) : ""
        );
        
        ZipEntry summaryEntry = new ZipEntry("DOWNLOAD_SUMMARY.txt");
        zipOutputStream.putNextEntry(summaryEntry);
        zipOutputStream.write(summaryContent.getBytes());
        zipOutputStream.closeEntry();
        
        zipOutputStream.close();
        
        log.info("ZIP archive created with {} successful and {} failed labels", successCount, failureCount);
        
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * Regenerate all missing shipping label URLs for shipped orders
     * Utility method to bulk update orders that have tracking but no label URL
     * 
     * @return Map containing regeneration statistics
     */
    public Map<String, Object> regenerateAllMissingLabels() {
        log.info("Starting regeneration of missing shipping labels");
        
        List<Order> shippedOrders = orderRepository.findByStatus(OrderStatus.SHIPPED);
        
        int totalWithTracking = 0;
        int labelUrlsGenerated = 0;
        int errors = 0;
        
        for (Order order : shippedOrders) {
            if (order.getTrackingNumber() != null && !order.getTrackingNumber().isEmpty()) {
                totalWithTracking++;
                
                try {
                    if (order.getShippingLabelUrl() == null || order.getShippingLabelUrl().isEmpty()) {
                        String labelUrl = delhiveryUtil.getShippingLabelUrl(order.getTrackingNumber());
                        order.setShippingLabelUrl(labelUrl);
                        orderRepository.save(order);
                        labelUrlsGenerated++;
                        log.debug("Generated label URL for order {}", order.getOrderNumber());
                    }
                } catch (Exception e) {
                    errors++;
                    log.warn("Failed to generate label URL for order {}: {}", order.getOrderNumber(), e.getMessage());
                }
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("totalOrdersChecked", shippedOrders.size());
        result.put("ordersWithTracking", totalWithTracking);
        result.put("labelUrlsGenerated", labelUrlsGenerated);
        result.put("errors", errors);
        
        log.info("Label regeneration completed: {} generated, {} errors", labelUrlsGenerated, errors);
        
        return result;
    }
}
