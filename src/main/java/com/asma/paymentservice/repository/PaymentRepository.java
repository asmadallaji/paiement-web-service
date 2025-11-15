package com.asma.paymentservice.repository;

import com.asma.paymentservice.entity.Payment;
import com.asma.paymentservice.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    /**
     * Finds an existing PENDING payment with the given orderId and userId.
     * Used for idempotency check to prevent duplicate payment creation.
     * 
     * @param orderId The order identifier
     * @param userId The user identifier
     * @param status The payment status (should be PENDING for idempotency check)
     * @return Optional containing the existing payment if found, empty otherwise
     */
    Optional<Payment> findByOrderIdAndUserIdAndStatus(String orderId, String userId, PaymentStatus status);
}

