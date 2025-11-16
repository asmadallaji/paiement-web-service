package com.asma.paymentservice.service;

import com.asma.paymentservice.entity.Invoice;
import com.asma.paymentservice.entity.InvoiceStatus;
import com.asma.paymentservice.entity.Payment;
import com.asma.paymentservice.entity.PaymentStatus;
import com.asma.paymentservice.exception.InvalidInvoiceRequestException;
import com.asma.paymentservice.exception.InvoiceNotFoundException;
import com.asma.paymentservice.exception.PaymentNotFoundException;
import com.asma.paymentservice.repository.InvoiceRepository;
import com.asma.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    
    // Simple sequence counter for invoice numbers (in production, use database sequence)
    private static final AtomicInteger sequenceCounter = new AtomicInteger(0);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    @Transactional
    public Invoice createInvoiceFromPayment(Payment payment) {
        // Check if invoice already exists for this payment (prevent duplicates)
        if (invoiceRepository.existsByPaymentId(payment.getId())) {
            log.warn("Invoice already exists for payment ID: {}. Skipping invoice creation.", payment.getId());
            return invoiceRepository.findByPaymentId(payment.getId())
                    .orElseThrow(() -> new IllegalStateException("Invoice exists but could not be retrieved for payment ID: " + payment.getId()));
        }

        // Generate unique invoice number
        String invoiceNumber = generateInvoiceNumber();

        // Create invoice entity with status CREATED
        Invoice invoice = Invoice.builder()
                .invoiceNumber(invoiceNumber)
                .paymentId(payment.getId())
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(InvoiceStatus.CREATED)
                .issueDate(LocalDate.now())
                .dueDate(null) // Optional, can be set later
                .orderId(payment.getOrderId())
                .build();

        // Save invoice to database
        Invoice savedInvoice = invoiceRepository.save(invoice);
        log.info("Invoice created with ID: {}, invoiceNumber: {}, for payment ID: {}", 
                savedInvoice.getId(), savedInvoice.getInvoiceNumber(), payment.getId());

        return savedInvoice;
    }

    public Invoice getInvoiceById(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Invoice not found with ID: {}", id);
                    return new InvoiceNotFoundException(id);
                });
    }

    public Invoice getInvoiceByPaymentId(Long paymentId) {
        return invoiceRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> {
                    log.warn("Invoice not found for payment ID: {}", paymentId);
                    return new InvoiceNotFoundException("Invoice not found for payment ID: " + paymentId);
                });
    }

    @Transactional
    public Invoice createInvoiceManually(Long paymentId) {
        // Validate payment exists and has status APPROVED
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> {
                    log.warn("Payment not found with ID: {}", paymentId);
                    return new PaymentNotFoundException(paymentId);
                });

        if (payment.getStatus() != PaymentStatus.APPROVED) {
            String message = String.format("Cannot create invoice for payment with status %s. Only APPROVED payments can have invoices.", payment.getStatus());
            log.warn("Invalid invoice creation request for payment ID {}: {}", paymentId, message);
            throw new InvalidInvoiceRequestException(message);
        }

        // Check if invoice already exists (return 409 if exists)
        if (invoiceRepository.existsByPaymentId(paymentId)) {
            String message = "Invoice already exists for payment ID: " + paymentId;
            log.warn("Duplicate invoice creation attempt for payment ID: {}", paymentId);
            throw new InvalidInvoiceRequestException(message);
        }

        // Create invoice using createInvoiceFromPayment
        return createInvoiceFromPayment(payment);
    }

    /**
     * Generates a unique invoice number using timestamp and sequence.
     * Format: INV-YYYYMMDD-HHMMSS-{sequence}
     */
    private String generateInvoiceNumber() {
        String timestamp = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        int sequence = sequenceCounter.incrementAndGet() % 10000; // Limit to 4 digits
        return String.format("INV-%s-%04d", timestamp, sequence);
    }
}

