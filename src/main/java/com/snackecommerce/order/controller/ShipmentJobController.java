package com.snackecommerce.order.controller;

import com.snackecommerce.order.entity.ShipmentJob;
import com.snackecommerce.order.enums.ShipmentJobStatus;
import com.snackecommerce.order.service.ShipmentJobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Admin endpoint for managing shipment retry jobs
 * Monitor and manually retry failed shipment creations
 */
@RestController
@RequestMapping("/api/admin/shipment-jobs")
public class ShipmentJobController {

    @Autowired
    private ShipmentJobService shipmentJobService;

    /**
     * GET /api/admin/shipment-jobs/pending
     * List all PENDING shipment jobs (waiting for retry)
     */
    @GetMapping("/pending")
    public ResponseEntity<Map<String, Object>> getPendingJobs() {
        List<ShipmentJob> pendingJobs = shipmentJobService.getPendingJobs();
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("count", pendingJobs.size());
        response.put("jobs", pendingJobs);
        
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/admin/shipment-jobs/failed
     * List all FAILED shipment jobs (max attempts reached)
     */
    @GetMapping("/failed")
    public ResponseEntity<Map<String, Object>> getFailedJobs() {
        List<ShipmentJob> failedJobs = shipmentJobService.getFailedJobs();
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("count", failedJobs.size());
        response.put("jobs", failedJobs);
        
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/admin/shipment-jobs/successful
     * List all SUCCESSFUL shipment jobs
     */
    @GetMapping("/successful")
    public ResponseEntity<Map<String, Object>> getSuccessfulJobs() {
        List<ShipmentJob> successfulJobs = shipmentJobService.getSuccessfulJobs();
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("count", successfulJobs.size());
        response.put("jobs", successfulJobs);
        
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/admin/shipment-jobs/{jobId}/retry
     * Manually retry a failed or pending shipment job
     * 
     * Request: (empty body)
     * Response: { "status": "success/failed", "message": "...", "job": {...} }
     */
    @PostMapping("/{jobId}/retry")
    public ResponseEntity<Map<String, Object>> retryShipmentJob(@PathVariable Long jobId) {
        try {
            ShipmentJob updatedJob = shipmentJobService.manuallyRetryJob(jobId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", updatedJob.getStatus().toString());
            response.put("message", updatedJob.getStatus() == ShipmentJobStatus.SUCCESS 
                ? "Shipment created successfully" 
                : "Retry failed, will be retried automatically");
            response.put("job", updatedJob);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Manual retry failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * GET /api/admin/shipment-jobs/stats
     * Get shipment job statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getShipmentJobStats() {
        List<ShipmentJob> pending = shipmentJobService.getPendingJobs();
        List<ShipmentJob> failed = shipmentJobService.getFailedJobs();
        List<ShipmentJob> successful = shipmentJobService.getSuccessfulJobs();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("pending", pending.size());
        stats.put("failed", failed.size());
        stats.put("successful", successful.size());
        stats.put("total", pending.size() + failed.size() + successful.size());
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("stats", stats);
        
        return ResponseEntity.ok(response);
    }
}
