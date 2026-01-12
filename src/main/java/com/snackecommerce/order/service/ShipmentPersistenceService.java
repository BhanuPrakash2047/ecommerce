package com.snackecommerce.order.service;

import com.snackecommerce.order.entity.Order;
import com.snackecommerce.order.entity.ShipmentJob;
import com.snackecommerce.order.enums.ShipmentJobStatus;
import com.snackecommerce.order.repository.ShipmentJobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.snackecommerce.notification.service.NotificationService;
import com.snackecommerce.order.repository.OrderRepository;

/**
 * Separate service for persisting ShipmentJob with REQUIRES_NEW transaction propagation.
 * This ensures database updates are committed independently from parent transaction,
 * preventing cascading rollbacks when exceptions occur.
 */
@Service
public class ShipmentPersistenceService {

    private static final Logger logger = LoggerFactory.getLogger(ShipmentPersistenceService.class);

    @Autowired
    private ShipmentJobRepository shipmentJobRepository;

    @Autowired
    private NotificationService notificationService;
    @Autowired
    private OrderRepository orderRepository;
    /**
     * Save shipment job in a new independent transaction.
     * Uses REQUIRES_NEW propagation to ensure the save commits regardless of parent transaction state.
     * 
     * @param job ShipmentJob to save
     * @return Saved ShipmentJob
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ShipmentJob saveJobInNewTransaction(ShipmentJob job) {
        ShipmentJob savedJob = shipmentJobRepository.saveAndFlush(job);
        logger.debug("Shipment job persisted in new transaction: id={}, orderId={}, attempts={}, status={}", 
                   savedJob.getId(), savedJob.getOrderId(), savedJob.getAttempts(), savedJob.getStatus());
        return savedJob;
    }

    /**
     * Increment attempt count and persist in new transaction asynchronously.
     * Used when shipment creation fails to ensure attempt count is saved.
     * 
     * @param job ShipmentJob to increment
     * @param errorMessage Error message from failed attempt
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void incrementAttemptAsync(ShipmentJob job, String errorMessage) {
        job.incrementAttempt(errorMessage);
        ShipmentJob savedJob = shipmentJobRepository.saveAndFlush(job);
        logger.info("Attempt incremented and persisted asynchronously: orderId={}, attempts={}, status={}", 
                   savedJob.getOrderId(), savedJob.getAttempts(), savedJob.getStatus());

        // Check if marked as failed after increment
        if (job.getStatus() == ShipmentJobStatus.FAILED) {
            logger.error("Shipment job marked FAILED for order {} after {} attempts",
                    job.getOrderId(), job.getAttempts());

            // Send admin alert notification about failed shipment
            try {
                Order order = orderRepository.findById(job.getOrderId()).orElse(null);
                if (order != null) {
                    Long adminUserId = 5L; // TODO: Make this configurable
                    notificationService.notifyAdminShipmentFailure(
                            adminUserId,
                            order.getId(),
                            order.getOrderNumber(),
                            job.getAttempts(),
                            job.getLastError()
                    );
                    logger.info("Admin notification sent for failed shipment (order {})", job.getOrderId());
                }
            } catch (Exception ex) {
                logger.error("Failed to send admin notification for shipment failure: {}", ex.getMessage());
            }
        }
    }

    /**
     * Save job with success status in new transaction.
     * 
     * @param job ShipmentJob to mark as success
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ShipmentJob saveSuccessfulJob(ShipmentJob job) {
        job.markSuccess();
        ShipmentJob savedJob = shipmentJobRepository.saveAndFlush(job);
        logger.info("Shipment job marked successful and persisted: orderId={}, waybill={}", 
                   savedJob.getOrderId(), savedJob.getId());
        return savedJob;
    }
}
