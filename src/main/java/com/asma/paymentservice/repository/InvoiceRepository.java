package com.asma.paymentservice.repository;

import com.asma.paymentservice.entity.Invoice;
import com.asma.paymentservice.entity.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    
    /**
     * Finds an invoice by payment ID.
     * 
     * @param paymentId The payment identifier
     * @return Optional containing the invoice if found, empty otherwise
     */
    Optional<Invoice> findByPaymentId(Long paymentId);
    
    /**
     * Checks if an invoice exists for a given payment ID.
     * 
     * @param paymentId The payment identifier
     * @return true if an invoice exists for this payment, false otherwise
     */
    boolean existsByPaymentId(Long paymentId);

    /**
     * Finds invoices filtered by status with pagination.
     * Results are ordered by issueDate descending (most recent first).
     */
    Page<Invoice> findByStatusOrderByIssueDateDesc(InvoiceStatus status, Pageable pageable);

    /**
     * Finds invoices filtered by userId with pagination.
     * Results are ordered by issueDate descending (most recent first).
     */
    Page<Invoice> findByUserIdOrderByIssueDateDesc(String userId, Pageable pageable);

    /**
     * Finds invoices filtered by status and userId with pagination.
     * Results are ordered by issueDate descending (most recent first).
     */
    Page<Invoice> findByStatusAndUserIdOrderByIssueDateDesc(InvoiceStatus status, String userId, Pageable pageable);

    /**
     * Finds invoices filtered by issueDate range with pagination.
     * Results are ordered by issueDate descending (most recent first).
     */
    Page<Invoice> findByIssueDateBetweenOrderByIssueDateDesc(LocalDate fromDate, LocalDate toDate, Pageable pageable);

    /**
     * Finds invoices filtered by status and issueDate range with pagination.
     * Results are ordered by issueDate descending (most recent first).
     */
    Page<Invoice> findByStatusAndIssueDateBetweenOrderByIssueDateDesc(InvoiceStatus status, LocalDate fromDate, LocalDate toDate, Pageable pageable);

    /**
     * Finds invoices filtered by userId and issueDate range with pagination.
     * Results are ordered by issueDate descending (most recent first).
     */
    Page<Invoice> findByUserIdAndIssueDateBetweenOrderByIssueDateDesc(String userId, LocalDate fromDate, LocalDate toDate, Pageable pageable);

    /**
     * Finds invoices filtered by status, userId, and issueDate range with pagination.
     * Results are ordered by issueDate descending (most recent first).
     */
    Page<Invoice> findByStatusAndUserIdAndIssueDateBetweenOrderByIssueDateDesc(InvoiceStatus status, String userId, LocalDate fromDate, LocalDate toDate, Pageable pageable);

    /**
     * Finds all invoices with pagination.
     * Results are ordered by issueDate descending (most recent first).
     */
    @Query("SELECT i FROM Invoice i ORDER BY i.issueDate DESC")
    Page<Invoice> findAllOrderByIssueDateDesc(Pageable pageable);
}

