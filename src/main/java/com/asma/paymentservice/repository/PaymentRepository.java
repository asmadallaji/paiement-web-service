package com.asma.paymentservice.repository;

import com.asma.paymentservice.entity.Payment;
import com.asma.paymentservice.entity.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    /**
     * Finds payments filtered by status with pagination.
     * Results are ordered by createdAt descending (most recent first).
     */
    Page<Payment> findByStatusOrderByCreatedAtDesc(PaymentStatus status, Pageable pageable);

    /**
     * Finds payments filtered by userId with pagination.
     * Results are ordered by createdAt descending (most recent first).
     */
    Page<Payment> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    /**
     * Finds payments filtered by orderId with pagination.
     * Results are ordered by createdAt descending (most recent first).
     */
    Page<Payment> findByOrderIdOrderByCreatedAtDesc(String orderId, Pageable pageable);

    /**
     * Finds payments filtered by status and userId with pagination.
     * Results are ordered by createdAt descending (most recent first).
     */
    Page<Payment> findByStatusAndUserIdOrderByCreatedAtDesc(PaymentStatus status, String userId, Pageable pageable);

    /**
     * Finds payments filtered by status and orderId with pagination.
     * Results are ordered by createdAt descending (most recent first).
     */
    Page<Payment> findByStatusAndOrderIdOrderByCreatedAtDesc(PaymentStatus status, String orderId, Pageable pageable);

    /**
     * Finds payments filtered by userId and orderId with pagination.
     * Results are ordered by createdAt descending (most recent first).
     */
    Page<Payment> findByUserIdAndOrderIdOrderByCreatedAtDesc(String userId, String orderId, Pageable pageable);

    /**
     * Finds payments filtered by status, userId, and orderId with pagination.
     * Results are ordered by createdAt descending (most recent first).
     */
    Page<Payment> findByStatusAndUserIdAndOrderIdOrderByCreatedAtDesc(PaymentStatus status, String userId, String orderId, Pageable pageable);

    /**
     * Finds all payments with pagination.
     * Results are ordered by createdAt descending (most recent first).
     */
    @Query("SELECT p FROM Payment p ORDER BY p.createdAt DESC")
    Page<Payment> findAllOrderByCreatedAtDesc(Pageable pageable);
}

