package com.snackecommerce.payment.repository;

import com.snackecommerce.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    /**
     * Find payment by Razorpay order ID
     */
    Optional<Payment> findByProviderOrderId(String providerOrderId);
    
    /**
     * Find payment by internal order ID
     */
    Optional<Payment> findByOrderId(Long orderId);
}

