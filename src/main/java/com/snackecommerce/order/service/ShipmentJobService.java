package com.snackecommerce.order.service;

import com.snackecommerce.delivery.service.DeliveryService;
import com.snackecommerce.order.entity.ShipmentJob;
import com.snackecommerce.order.enums.ShipmentJobStatus;
import com.snackecommerce.order.repository.ShipmentJobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ShipmentJobService {

    private static final Logger logger = LoggerFactory.getLogger(ShipmentJobService.class);

    @Autowired
    private ShipmentJobRepository shipmentJobRepository;

    @Autowired
    private DeliveryService deliveryService;

    /**
     * Save a failed shipment creation as a retry job
     * 
     * @param orderId Order ID
     * @param error Error message from shipment creation failure
     * @return ShipmentJob entity
     */
    public ShipmentJob saveFailedShipment(Long orderId, String error) {
        // Check if job already exists for this order
        Optional<ShipmentJob> existingJob = shipmentJobRepository.findByOrderId(orderId);
        
        ShipmentJob job;
        if (existingJob.isPresent()) {
            job = existingJob.get();
            job.incrementAttempt(error);
        } else {
            job = ShipmentJob.builder()
                    .orderId(orderId)
                    .status(ShipmentJobStatus.PENDING)
                    .attempts(0)
                    .lastError(error)
                    .nextRetryAt(LocalDateTime.now().plusMinutes(5)) // First retry after 5 minutes
                    .build();
        }
        
        ShipmentJob savedJob = shipmentJobRepository.save(job);
        logger.info("Shipment job created/updated for order {}: attempt {}, next retry at {}", 
                   orderId, savedJob.getAttempts(), savedJob.getNextRetryAt());
        return savedJob;
    }

    /**
     * Scheduled task to retry failed shipment creation
     * Runs every 2 minutes
     * Picks up jobs with status=PENDING and nextRetryAt <= now
     * Max 5 retries per order, then marked FAILED
     */
    @Scheduled(fixedDelay = 120000) // 2 minutes
    public void retryFailedShipments() {
        logger.info("Shipment retry scheduler started");
        
        List<ShipmentJob> pendingJobs = shipmentJobRepository.findPendingJobsReadyForRetry();
        logger.info("Found {} pending shipment jobs ready for retry", pendingJobs.size());
        
        for (ShipmentJob job : pendingJobs) {
            try {
                logger.info("Retrying shipment for order {} (attempt {})", job.getOrderId(), job.getAttempts() + 1);
                
                // Try to create shipment
                String waybill = deliveryService.createShipment(job.getOrderId());
                
                // Success!
                job.markSuccess();
                shipmentJobRepository.save(job);
                logger.info("Shipment created successfully for order {} with waybill: {}", job.getOrderId(), waybill);
                
            } catch (Exception e) {
                // Increment attempt and schedule next retry or mark as failed
                logger.warn("Shipment retry failed for order {}: {}", job.getOrderId(), e.getMessage());
                job.incrementAttempt(e.getMessage());
                shipmentJobRepository.save(job);
                
                if (job.getStatus() == ShipmentJobStatus.FAILED) {
                    logger.error("Shipment job marked FAILED for order {} after {} attempts", 
                               job.getOrderId(), job.getAttempts());
                    // TODO: Send alert to ops/admin about failed shipment
                }
            }
        }
        
        logger.info("Shipment retry scheduler completed");
    }

    /**
     * Get all pending shipment jobs
     */
    public List<ShipmentJob> getPendingJobs() {
        return shipmentJobRepository.findByStatus(ShipmentJobStatus.PENDING);
    }

    /**
     * Get all failed shipment jobs
     */
    public List<ShipmentJob> getFailedJobs() {
        return shipmentJobRepository.findByStatusOrderByUpdatedAtDesc(ShipmentJobStatus.FAILED);
    }

    /**
     * Get all successful shipment jobs
     */
    public List<ShipmentJob> getSuccessfulJobs() {
        return shipmentJobRepository.findByStatusOrderByUpdatedAtDesc(ShipmentJobStatus.SUCCESS);
    }

    /**
     * Manually retry a failed shipment job
     * 
     * @param jobId Job ID
     * @return Updated ShipmentJob
     */
    public ShipmentJob manuallyRetryJob(Long jobId) {
        ShipmentJob job = shipmentJobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Shipment job not found: " + jobId));
        
        try {
            logger.info("Manually retrying shipment for order {} (job {})", job.getOrderId(), jobId);
            
            String waybill = deliveryService.createShipment(job.getOrderId());
            job.markSuccess();
            shipmentJobRepository.save(job);
            logger.info("Manual retry successful for order {} with waybill: {}", job.getOrderId(), waybill);
            
        } catch (Exception e) {
            logger.warn("Manual retry failed for order {}: {}", job.getOrderId(), e.getMessage());
            job.incrementAttempt(e.getMessage());
            shipmentJobRepository.save(job);
        }
        
        return job;
    }
}
