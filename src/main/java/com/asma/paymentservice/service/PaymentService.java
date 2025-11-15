package com.asma.paymentservice.service;

import com.asma.paymentservice.dto.CreatePaymentRequest;
import com.asma.paymentservice.dto.PaymentResponse;
import com.asma.paymentservice.entity.Payment;
import com.asma.paymentservice.entity.PaymentStatus;
import com.asma.paymentservice.exception.InvalidPaymentRequestException;
import com.asma.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;

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
}

