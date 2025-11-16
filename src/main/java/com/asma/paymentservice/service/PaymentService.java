package com.asma.paymentservice.service;

import com.asma.paymentservice.dto.CreatePaymentRequest;
import com.asma.paymentservice.dto.PaymentListResponse;
import com.asma.paymentservice.dto.PaymentResponse;
import com.asma.paymentservice.entity.Payment;
import com.asma.paymentservice.entity.PaymentStatus;
import com.asma.paymentservice.exception.InvalidPaymentRequestException;
import com.asma.paymentservice.exception.InvalidStatusTransitionException;
import com.asma.paymentservice.exception.PaymentNotFoundException;
import com.asma.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final InvoiceService invoiceService;

    @Transactional
    public PaymentResponse createPayment(CreatePaymentRequest request) {
        validatePaymentRequest(request);

        // Check for existing PENDING payment with same orderId + userId (idempotency check)
        String orderId = request.getOrderId() != null && request.getOrderId().isPresent() 
                ? request.getOrderId().get() : null;
        
        if (orderId != null) {
            Optional<Payment> existingPayment = paymentRepository.findByOrderIdAndUserIdAndStatus(
                    orderId, request.getUserId(), PaymentStatus.PENDING);
            
            if (existingPayment.isPresent()) {
                log.info("Duplicate payment creation detected for orderId: {} and userId: {}. Returning existing payment with ID: {}", 
                        orderId, request.getUserId(), existingPayment.get().getId());
                return mapToResponse(existingPayment.get());
            }
        }

        Payment payment = Payment.builder()
                .amount(BigDecimal.valueOf(request.getAmount()))
                .currency(request.getCurrency())
                .method(request.getMethod().getValue())
                .status(PaymentStatus.PENDING)
                .userId(request.getUserId())
                .orderId(orderId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        log.info("Payment created with ID: {}", savedPayment.getId());

        return mapToResponse(savedPayment);
    }

    public PaymentResponse getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Payment not found with ID: {}", id);
                    return new PaymentNotFoundException(id);
                });
        
        log.info("Payment retrieved with ID: {}", id);
        return mapToResponse(payment);
    }

    public PaymentListResponse listPayments(String status, String userId, String orderId, Integer page, Integer size) {
        // Validate and set default pagination parameters
        int pageNumber = (page != null && page >= 0) ? page : 0;
        int pageSize = (size != null && size >= 1 && size <= 100) ? size : 20;

        if (page != null && page < 0) {
            throw new InvalidPaymentRequestException("Page number must be >= 0");
        }
        if (size != null && (size < 1 || size > 100)) {
            throw new InvalidPaymentRequestException("Page size must be between 1 and 100");
        }

        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<Payment> paymentPage;

        // Convert status string to enum if provided
        PaymentStatus statusEnum = null;
        if (status != null && !status.trim().isEmpty()) {
            try {
                statusEnum = PaymentStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new InvalidPaymentRequestException("Invalid status value: " + status);
            }
        }

        // Select appropriate repository method based on provided filters
        if (statusEnum != null && userId != null && !userId.trim().isEmpty() && orderId != null && !orderId.trim().isEmpty()) {
            // All three filters
            paymentPage = paymentRepository.findByStatusAndUserIdAndOrderIdOrderByCreatedAtDesc(statusEnum, userId, orderId, pageable);
            log.info("Listing payments with filters: status={}, userId={}, orderId={}, page={}, size={}", statusEnum, userId, orderId, pageNumber, pageSize);
        } else if (statusEnum != null && userId != null && !userId.trim().isEmpty()) {
            // Status and userId
            paymentPage = paymentRepository.findByStatusAndUserIdOrderByCreatedAtDesc(statusEnum, userId, pageable);
            log.info("Listing payments with filters: status={}, userId={}, page={}, size={}", statusEnum, userId, pageNumber, pageSize);
        } else if (statusEnum != null && orderId != null && !orderId.trim().isEmpty()) {
            // Status and orderId
            paymentPage = paymentRepository.findByStatusAndOrderIdOrderByCreatedAtDesc(statusEnum, orderId, pageable);
            log.info("Listing payments with filters: status={}, orderId={}, page={}, size={}", statusEnum, orderId, pageNumber, pageSize);
        } else if (userId != null && !userId.trim().isEmpty() && orderId != null && !orderId.trim().isEmpty()) {
            // userId and orderId
            paymentPage = paymentRepository.findByUserIdAndOrderIdOrderByCreatedAtDesc(userId, orderId, pageable);
            log.info("Listing payments with filters: userId={}, orderId={}, page={}, size={}", userId, orderId, pageNumber, pageSize);
        } else if (statusEnum != null) {
            // Only status
            paymentPage = paymentRepository.findByStatusOrderByCreatedAtDesc(statusEnum, pageable);
            log.info("Listing payments with filters: status={}, page={}, size={}", statusEnum, pageNumber, pageSize);
        } else if (userId != null && !userId.trim().isEmpty()) {
            // Only userId
            paymentPage = paymentRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
            log.info("Listing payments with filters: userId={}, page={}, size={}", userId, pageNumber, pageSize);
        } else if (orderId != null && !orderId.trim().isEmpty()) {
            // Only orderId
            paymentPage = paymentRepository.findByOrderIdOrderByCreatedAtDesc(orderId, pageable);
            log.info("Listing payments with filters: orderId={}, page={}, size={}", orderId, pageNumber, pageSize);
        } else {
            // No filters
            paymentPage = paymentRepository.findAllOrderByCreatedAtDesc(pageable);
            log.info("Listing all payments, page={}, size={}", pageNumber, pageSize);
        }

        return mapToPaymentListResponse(paymentPage);
    }

    @Transactional
    public PaymentResponse updatePaymentStatus(Long id, PaymentStatus newStatus) {
        // Fetch payment and validate existence
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Payment not found with ID: {}", id);
                    return new PaymentNotFoundException(id);
                });

        // Validate status transition using centralized validation
        validateStatusTransition(id, payment.getStatus(), newStatus);

        // Update payment status and timestamp
        payment.setStatus(newStatus);
        payment.setUpdatedAt(LocalDateTime.now());

        // Save updated payment
        Payment savedPayment = paymentRepository.save(payment);
        log.info("Payment status updated for ID {}: {} -> {}", id, payment.getStatus(), newStatus);

        // Automatically create invoice when payment transitions to APPROVED
        if (newStatus == PaymentStatus.APPROVED) {
            try {
                invoiceService.createInvoiceFromPayment(savedPayment);
                log.info("Invoice automatically created for approved payment ID: {}", id);
            } catch (Exception e) {
                // Log error but don't fail payment update (as per design decision)
                log.error("Failed to create invoice for approved payment ID: {}. Error: {}", id, e.getMessage(), e);
            }
        }

        return mapToResponse(savedPayment);
    }

    private void validatePaymentRequest(CreatePaymentRequest request) {
        if (request.getAmount() == null || request.getAmount() <= 0) {
            throw new InvalidPaymentRequestException("Amount must be greater than 0");
        }

        if (request.getCurrency() == null || request.getCurrency().trim().isEmpty()) {
            throw new InvalidPaymentRequestException("Currency must not be empty");
        }

        if (request.getMethod() == null) {
            throw new InvalidPaymentRequestException("Payment method is required");
        }

        if (request.getUserId() == null || request.getUserId().trim().isEmpty()) {
            throw new InvalidPaymentRequestException("UserId must not be empty");
        }
    }

    /**
     * Validates a payment status transition according to business rules.
     * Only transitions from PENDING to APPROVED, FAILED, or CANCELED are allowed.
     * Terminal states (APPROVED, FAILED, CANCELED) cannot be changed.
     *
     * @param paymentId The payment ID (for logging purposes)
     * @param currentStatus The current payment status
     * @param targetStatus The target payment status
     * @throws InvalidStatusTransitionException if the transition is invalid
     */
    public void validateStatusTransition(Long paymentId, PaymentStatus currentStatus, PaymentStatus targetStatus) {
        // Validate target status is not null
        if (targetStatus == null) {
            String message = "Target status cannot be null";
            log.warn("Invalid status transition for payment ID {}: {} -> null. Reason: {}", 
                    paymentId, currentStatus, message);
            throw new InvalidStatusTransitionException(message);
        }

        // Reject same-status transitions
        if (currentStatus == targetStatus) {
            String message = String.format("Cannot transition from %s to %s (same status)", currentStatus, targetStatus);
            log.warn("Invalid status transition for payment ID {}: {} -> {}. Reason: {}", 
                    paymentId, currentStatus, targetStatus, message);
            throw new InvalidStatusTransitionException(message);
        }

        // Terminal states cannot be changed
        if (currentStatus == PaymentStatus.APPROVED || 
            currentStatus == PaymentStatus.FAILED || 
            currentStatus == PaymentStatus.CANCELED) {
            String message = String.format("Cannot transition from terminal state %s to %s. Payments in %s status cannot be modified.", 
                    currentStatus, targetStatus, currentStatus);
            log.warn("Invalid status transition for payment ID {}: {} -> {}. Reason: {}", 
                    paymentId, currentStatus, targetStatus, message);
            throw new InvalidStatusTransitionException(message);
        }

        // Only PENDING can transition, and only to APPROVED, FAILED, or CANCELED
        if (currentStatus == PaymentStatus.PENDING) {
            if (targetStatus != PaymentStatus.APPROVED && 
                targetStatus != PaymentStatus.FAILED && 
                targetStatus != PaymentStatus.CANCELED) {
                String message = String.format("Invalid transition from PENDING to %s. PENDING payments can only transition to APPROVED, FAILED, or CANCELED.", 
                        targetStatus);
                log.warn("Invalid status transition for payment ID {}: {} -> {}. Reason: {}", 
                        paymentId, currentStatus, targetStatus, message);
                throw new InvalidStatusTransitionException(message);
            }
        }
    }

    private PaymentResponse mapToResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setAmount(payment.getAmount().doubleValue());
        response.setCurrency(payment.getCurrency());
        response.setMethod(payment.getMethod());
        response.setStatus(PaymentResponse.StatusEnum.fromValue(payment.getStatus().name()));
        response.setUserId(payment.getUserId());
        response.setOrderId(payment.getOrderId() != null ? JsonNullable.of(payment.getOrderId()) : JsonNullable.undefined());
        response.setCreatedAt(payment.getCreatedAt().atOffset(ZoneOffset.UTC));
        response.setUpdatedAt(payment.getUpdatedAt().atOffset(ZoneOffset.UTC));
        return response;
    }

    private PaymentListResponse mapToPaymentListResponse(Page<Payment> paymentPage) {
        PaymentListResponse response = new PaymentListResponse();
        
        List<PaymentResponse> content = paymentPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        response.setContent(content);
        
        response.setTotalElements(paymentPage.getTotalElements());
        response.setTotalPages(paymentPage.getTotalPages());
        response.setPage(paymentPage.getNumber());
        response.setSize(paymentPage.getSize());
        
        return response;
    }
}

