package com.snackecommerce.order.repository;

import com.snackecommerce.order.entity.ShipmentJob;
import com.snackecommerce.order.enums.ShipmentJobStatus;
//import com.snackecommerce.order.enums.ShipmentJobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShipmentJobRepository extends JpaRepository<ShipmentJob, Long> {

    Optional<ShipmentJob> findByOrderId(Long orderId);

    List<ShipmentJob> findByStatus(ShipmentJobStatus status);

    List<ShipmentJob> findByStatusAndNextRetryAtIsNotNull(ShipmentJobStatus status);

    /**
     * Find all PENDING jobs that are ready for retry (nextRetryAt <= now)
     */
    @Query("SELECT j FROM ShipmentJob j WHERE j.status = 'PENDING' AND j.nextRetryAt IS NOT NULL AND j.nextRetryAt <= CURRENT_TIMESTAMP")
    List<ShipmentJob> findPendingJobsReadyForRetry();

    List<ShipmentJob> findByStatusOrderByUpdatedAtDesc(ShipmentJobStatus status);

}
