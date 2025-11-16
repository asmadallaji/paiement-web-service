package com.asma.paymentservice.service;

import com.asma.paymentservice.entity.Invoice;
import com.asma.paymentservice.entity.InvoiceStatus;
import com.asma.paymentservice.entity.Payment;
import com.asma.paymentservice.entity.PaymentStatus;
import com.asma.paymentservice.exception.InvalidInvoiceRequestException;
import com.asma.paymentservice.exception.InvoiceNotFoundException;
import com.asma.paymentservice.exception.InvalidStatusTransitionException;
import com.asma.paymentservice.exception.PaymentNotFoundException;
import com.asma.paymentservice.repository.InvoiceRepository;
import com.asma.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
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

    @Transactional
    public Invoice updateInvoiceStatus(Long id, InvoiceStatus newStatus) {
        // Fetch invoice and validate existence
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Invoice not found with ID: {}", id);
                    return new InvoiceNotFoundException(id);
                });

        // Validate status transition using centralized validation
        validateStatusTransition(id, invoice.getStatus(), newStatus);

        // Update invoice status
        invoice.setStatus(newStatus);

        // Set appropriate date fields when status changes
        LocalDate now = LocalDate.now();
        if (newStatus == InvoiceStatus.SENT && invoice.getSentAt() == null) {
            invoice.setSentAt(now);
        } else if (newStatus == InvoiceStatus.PAID && invoice.getPaidAt() == null) {
            invoice.setPaidAt(now);
        } else if (newStatus == InvoiceStatus.CANCELLED && invoice.getCancelledAt() == null) {
            invoice.setCancelledAt(now);
        }

        // Save updated invoice
        Invoice savedInvoice = invoiceRepository.save(invoice);
        log.info("Invoice status updated for ID {}: {} -> {}", id, invoice.getStatus(), newStatus);

        return savedInvoice;
    }

    /**
     * Validates an invoice status transition according to business rules.
     * Allowed transitions: CREATED → SENT, CREATED → PAID, CREATED → CANCELLED, SENT → PAID
     * Terminal states (PAID, CANCELLED) cannot be changed.
     * Backward transitions are not allowed.
     *
     * @param invoiceId The invoice ID (for logging purposes)
     * @param currentStatus The current invoice status
     * @param targetStatus The target invoice status
     * @throws InvalidStatusTransitionException if the transition is invalid
     */
    public void validateStatusTransition(Long invoiceId, InvoiceStatus currentStatus, InvoiceStatus targetStatus) {
        // Validate target status is not null
        if (targetStatus == null) {
            String message = "Target status cannot be null";
            log.warn("Invalid status transition for invoice ID {}: {} -> null. Reason: {}", 
                    invoiceId, currentStatus, message);
            throw new InvalidStatusTransitionException(message);
        }

        // Reject same-status transitions
        if (currentStatus == targetStatus) {
            String message = String.format("Cannot transition from %s to %s (same status)", currentStatus, targetStatus);
            log.warn("Invalid status transition for invoice ID {}: {} -> {}. Reason: {}", 
                    invoiceId, currentStatus, targetStatus, message);
            throw new InvalidStatusTransitionException(message);
        }

        // Terminal states cannot be changed
        if (currentStatus == InvoiceStatus.PAID || currentStatus == InvoiceStatus.CANCELLED) {
            String message = String.format("Cannot transition from terminal state %s to %s. Invoices in %s status cannot be modified.", 
                    currentStatus, targetStatus, currentStatus);
            log.warn("Invalid status transition for invoice ID {}: {} -> {}. Reason: {}", 
                    invoiceId, currentStatus, targetStatus, message);
            throw new InvalidStatusTransitionException(message);
        }

        // Validate allowed transitions based on current status
        if (currentStatus == InvoiceStatus.CREATED) {
            // CREATED can transition to SENT, PAID, or CANCELLED
            if (targetStatus != InvoiceStatus.SENT && 
                targetStatus != InvoiceStatus.PAID && 
                targetStatus != InvoiceStatus.CANCELLED) {
                String message = String.format("Invalid transition from CREATED to %s. CREATED invoices can only transition to SENT, PAID, or CANCELLED.", 
                        targetStatus);
                log.warn("Invalid status transition for invoice ID {}: {} -> {}. Reason: {}", 
                        invoiceId, currentStatus, targetStatus, message);
                throw new InvalidStatusTransitionException(message);
            }
        } else if (currentStatus == InvoiceStatus.SENT) {
            // SENT can only transition to PAID (not backward to CREATED)
            if (targetStatus != InvoiceStatus.PAID) {
                String message = String.format("Invalid transition from SENT to %s. SENT invoices can only transition to PAID.", 
                        targetStatus);
                log.warn("Invalid status transition for invoice ID {}: {} -> {}. Reason: {}", 
                        invoiceId, currentStatus, targetStatus, message);
                throw new InvalidStatusTransitionException(message);
            }
        }
    }

    public com.asma.paymentservice.dto.InvoiceListResponse listInvoices(String status, String userId, LocalDate fromDate, LocalDate toDate, Integer page, Integer size) {
        // Validate and set default pagination parameters
        int pageNumber = (page != null && page >= 0) ? page : 0;
        int pageSize = (size != null && size >= 1 && size <= 100) ? size : 20;

        if (page != null && page < 0) {
            throw new InvalidInvoiceRequestException("Page number must be >= 0");
        }
        if (size != null && (size < 1 || size > 100)) {
            throw new InvalidInvoiceRequestException("Page size must be between 1 and 100");
        }

        // Validate date range
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new InvalidInvoiceRequestException("fromDate must be <= toDate");
        }

        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<Invoice> invoicePage;

        // Convert status string to enum if provided
        InvoiceStatus statusEnum = null;
        if (status != null && !status.trim().isEmpty()) {
            try {
                statusEnum = InvoiceStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new InvalidInvoiceRequestException("Invalid status value: " + status);
            }
        }

        // Select appropriate repository method based on provided filters
        if (statusEnum != null && userId != null && !userId.trim().isEmpty() && fromDate != null && toDate != null) {
            // All three filters: status, userId, date range
            invoicePage = invoiceRepository.findByStatusAndUserIdAndIssueDateBetweenOrderByIssueDateDesc(statusEnum, userId, fromDate, toDate, pageable);
            log.info("Listing invoices with filters: status={}, userId={}, fromDate={}, toDate={}, page={}, size={}", statusEnum, userId, fromDate, toDate, pageNumber, pageSize);
        } else if (statusEnum != null && userId != null && !userId.trim().isEmpty()) {
            // Status and userId
            invoicePage = invoiceRepository.findByStatusAndUserIdOrderByIssueDateDesc(statusEnum, userId, pageable);
            log.info("Listing invoices with filters: status={}, userId={}, page={}, size={}", statusEnum, userId, pageNumber, pageSize);
        } else if (statusEnum != null && fromDate != null && toDate != null) {
            // Status and date range
            invoicePage = invoiceRepository.findByStatusAndIssueDateBetweenOrderByIssueDateDesc(statusEnum, fromDate, toDate, pageable);
            log.info("Listing invoices with filters: status={}, fromDate={}, toDate={}, page={}, size={}", statusEnum, fromDate, toDate, pageNumber, pageSize);
        } else if (userId != null && !userId.trim().isEmpty() && fromDate != null && toDate != null) {
            // userId and date range
            invoicePage = invoiceRepository.findByUserIdAndIssueDateBetweenOrderByIssueDateDesc(userId, fromDate, toDate, pageable);
            log.info("Listing invoices with filters: userId={}, fromDate={}, toDate={}, page={}, size={}", userId, fromDate, toDate, pageNumber, pageSize);
        } else if (statusEnum != null) {
            // Only status
            invoicePage = invoiceRepository.findByStatusOrderByIssueDateDesc(statusEnum, pageable);
            log.info("Listing invoices with filters: status={}, page={}, size={}", statusEnum, pageNumber, pageSize);
        } else if (userId != null && !userId.trim().isEmpty()) {
            // Only userId
            invoicePage = invoiceRepository.findByUserIdOrderByIssueDateDesc(userId, pageable);
            log.info("Listing invoices with filters: userId={}, page={}, size={}", userId, pageNumber, pageSize);
        } else if (fromDate != null && toDate != null) {
            // Only date range
            invoicePage = invoiceRepository.findByIssueDateBetweenOrderByIssueDateDesc(fromDate, toDate, pageable);
            log.info("Listing invoices with filters: fromDate={}, toDate={}, page={}, size={}", fromDate, toDate, pageNumber, pageSize);
        } else {
            // No filters
            invoicePage = invoiceRepository.findAllOrderByIssueDateDesc(pageable);
            log.info("Listing all invoices, page={}, size={}", pageNumber, pageSize);
        }

        return mapToInvoiceListResponse(invoicePage);
    }

    private com.asma.paymentservice.dto.InvoiceListResponse mapToInvoiceListResponse(Page<Invoice> invoicePage) {
        com.asma.paymentservice.dto.InvoiceListResponse response = new com.asma.paymentservice.dto.InvoiceListResponse();
        
        List<com.asma.paymentservice.dto.InvoiceResponse> content = invoicePage.getContent().stream()
                .map(this::mapToInvoiceResponse)
                .collect(Collectors.toList());
        response.setContent(content);
        
        response.setTotalElements(invoicePage.getTotalElements());
        response.setTotalPages(invoicePage.getTotalPages());
        response.setPage(invoicePage.getNumber());
        response.setSize(invoicePage.getSize());
        
        return response;
    }

    private com.asma.paymentservice.dto.InvoiceResponse mapToInvoiceResponse(Invoice invoice) {
        com.asma.paymentservice.dto.InvoiceResponse response = new com.asma.paymentservice.dto.InvoiceResponse();
        response.setId(invoice.getId());
        response.setInvoiceNumber(invoice.getInvoiceNumber());
        response.setPaymentId(invoice.getPaymentId());
        response.setUserId(invoice.getUserId());
        response.setAmount(invoice.getAmount().doubleValue());
        response.setCurrency(invoice.getCurrency());
        response.setStatus(com.asma.paymentservice.dto.InvoiceResponse.StatusEnum.fromValue(invoice.getStatus().name()));
        response.setIssueDate(invoice.getIssueDate());
        if (invoice.getDueDate() != null) {
            response.setDueDate(org.openapitools.jackson.nullable.JsonNullable.of(invoice.getDueDate()));
        }
        if (invoice.getSentAt() != null) {
            response.setSentAt(org.openapitools.jackson.nullable.JsonNullable.of(invoice.getSentAt()));
        }
        if (invoice.getPaidAt() != null) {
            response.setPaidAt(org.openapitools.jackson.nullable.JsonNullable.of(invoice.getPaidAt()));
        }
        if (invoice.getCancelledAt() != null) {
            response.setCancelledAt(org.openapitools.jackson.nullable.JsonNullable.of(invoice.getCancelledAt()));
        }
        response.setOrderId(invoice.getOrderId() != null 
                ? org.openapitools.jackson.nullable.JsonNullable.of(invoice.getOrderId()) 
                : org.openapitools.jackson.nullable.JsonNullable.undefined());
        return response;
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

