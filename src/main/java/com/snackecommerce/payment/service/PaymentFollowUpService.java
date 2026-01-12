package com.snackecommerce.payment.service;

import com.snackecommerce.delivery.service.DeliveryService;
import com.snackecommerce.notification.service.NotificationService;
import com.snackecommerce.order.entity.Order;
import com.snackecommerce.order.service.ShipmentJobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Handles asynchronous follow-up operations after payment success
 * Runs AFTER main payment transaction commits
 * Failures here do NOT affect payment confirmation
 */
@Service
public class PaymentFollowUpService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentFollowUpService.class);

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private DeliveryService deliveryService;

    @Autowired
    private ShipmentJobService shipmentJobService;

    /**
     * Send payment success notification asynchronously
     * Runs in separate thread - main transaction has already committed
     * Failures are logged but don't affect payment
     */
    @Async
    public void notifyPaymentSuccess(Order order) {
        try {
            notificationService.notifyPaymentReceived(
                order.getUserId(),
                order.getId(),
                order.getOrderNumber(),
                order.getTotalAmount()
            );
            logger.info("✅ Notification sent to user {} for payment received", order.getUserId());
        } catch (Exception e) {
            logger.error("❌ Failed to send payment notification: {}", e.getMessage(), e);
            // Failure is logged but doesn't affect main transaction - it already committed
        }
    }

    /**
     * Create shipment asynchronously
     * Runs in separate thread - main transaction has already committed
     * Failed shipments are saved to retry queue for automatic retry
     */
    @Async
    public void createShipmentAsync(Order order) {
        try {
            String waybill = deliveryService.createShipment(order.getId());
            logger.info("✅ Shipment created successfully for order ID: {} with waybill: {}", order.getId(), waybill);
        } catch (Exception e) {
            logger.error("❌ Failed to create shipment for order ID: {}. Saving to retry queue.", order.getId(), e);
            // Save failed shipment as a retry job - will be retried automatically by scheduler
            try {
                shipmentJobService.saveFailedShipment(order.getId(), e.getMessage());
                logger.info("✅ Shipment job created for order {} - will retry automatically", order.getId());
            } catch (Exception jobException) {
                logger.error("❌ Failed to save shipment retry job: {}", jobException.getMessage(), jobException);
            }
        }
    }
}
