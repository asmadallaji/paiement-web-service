package com.asma.paymentservice.repository;

import com.asma.paymentservice.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}

