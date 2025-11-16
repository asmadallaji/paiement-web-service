package com.asma.paymentservice.service;

import com.asma.paymentservice.dto.CreatePaymentRequest;
import org.openapitools.jackson.nullable.JsonNullable;
import com.asma.paymentservice.dto.PaymentListResponse;
import com.asma.paymentservice.dto.PaymentResponse;
import com.asma.paymentservice.entity.Payment;
import com.asma.paymentservice.entity.PaymentStatus;
import com.asma.paymentservice.exception.InvalidPaymentRequestException;
import com.asma.paymentservice.exception.InvalidStatusTransitionException;
import com.asma.paymentservice.exception.PaymentNotFoundException;
import com.asma.paymentservice.repository.PaymentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    private CreatePaymentRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new CreatePaymentRequest();
        validRequest.setAmount(99.99);
        validRequest.setCurrency("USD");
        validRequest.setMethod(CreatePaymentRequest.MethodEnum.CREDIT_CARD);
        validRequest.setUserId("user123");
        validRequest.setOrderId(JsonNullable.of("order456"));
    }

    @Test
    void createPayment_WithValidRequest_ShouldCreatePayment() {
        // Given
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId(1L);
            return payment;
        });

        // When
        PaymentResponse response = paymentService.createPayment(validRequest);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(99.99, response.getAmount());
        assertEquals("USD", response.getCurrency());
        assertEquals("CREDIT_CARD", response.getMethod());
        assertEquals(PaymentResponse.StatusEnum.PENDING, response.getStatus());
        assertEquals("user123", response.getUserId());
        assertTrue(response.getOrderId().isPresent());
        assertEquals("order456", response.getOrderId().get());
        assertNotNull(response.getCreatedAt());
        assertNotNull(response.getUpdatedAt());

        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void createPayment_WithValidRequestWithoutOrderId_ShouldCreatePayment() {
        // Given
        validRequest.setOrderId(JsonNullable.undefined());
        Payment savedPayment = Payment.builder()
                .id(1L)
                .amount(BigDecimal.valueOf(validRequest.getAmount()))
                .currency(validRequest.getCurrency())
                .method(validRequest.getMethod().getValue())
                .status(PaymentStatus.PENDING)
                .userId(validRequest.getUserId())
                .orderId(null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        // When
        PaymentResponse response = paymentService.createPayment(validRequest);

        // Then
        assertNotNull(response);
        assertFalse(response.getOrderId().isPresent());
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void createPayment_WithNegativeAmount_ShouldThrowException() {
        // Given
        validRequest.setAmount(-10.00);

        // When/Then
        InvalidPaymentRequestException exception = assertThrows(
                InvalidPaymentRequestException.class,
                () -> paymentService.createPayment(validRequest)
        );

        assertEquals("Amount must be greater than 0", exception.getMessage());
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void createPayment_WithZeroAmount_ShouldThrowException() {
        // Given
        validRequest.setAmount(0.0);

        // When/Then
        InvalidPaymentRequestException exception = assertThrows(
                InvalidPaymentRequestException.class,
                () -> paymentService.createPayment(validRequest)
        );

        assertEquals("Amount must be greater than 0", exception.getMessage());
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void createPayment_WithNullAmount_ShouldThrowException() {
        // Given
        validRequest.setAmount(null);

        // When/Then
        InvalidPaymentRequestException exception = assertThrows(
                InvalidPaymentRequestException.class,
                () -> paymentService.createPayment(validRequest)
        );

        assertEquals("Amount must be greater than 0", exception.getMessage());
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void createPayment_WithEmptyCurrency_ShouldThrowException() {
        // Given
        validRequest.setCurrency("");

        // When/Then
        InvalidPaymentRequestException exception = assertThrows(
                InvalidPaymentRequestException.class,
                () -> paymentService.createPayment(validRequest)
        );

        assertEquals("Currency must not be empty", exception.getMessage());
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void createPayment_WithNullCurrency_ShouldThrowException() {
        // Given
        validRequest.setCurrency(null);

        // When/Then
        InvalidPaymentRequestException exception = assertThrows(
                InvalidPaymentRequestException.class,
                () -> paymentService.createPayment(validRequest)
        );

        assertEquals("Currency must not be empty", exception.getMessage());
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void createPayment_WithNullMethod_ShouldThrowException() {
        // Given
        validRequest.setMethod(null);

        // When/Then
        InvalidPaymentRequestException exception = assertThrows(
                InvalidPaymentRequestException.class,
                () -> paymentService.createPayment(validRequest)
        );

        assertEquals("Payment method is required", exception.getMessage());
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void createPayment_WithEmptyUserId_ShouldThrowException() {
        // Given
        validRequest.setUserId("");

        // When/Then
        InvalidPaymentRequestException exception = assertThrows(
                InvalidPaymentRequestException.class,
                () -> paymentService.createPayment(validRequest)
        );

        assertEquals("UserId must not be empty", exception.getMessage());
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void createPayment_WithNullUserId_ShouldThrowException() {
        // Given
        validRequest.setUserId(null);

        // When/Then
        InvalidPaymentRequestException exception = assertThrows(
                InvalidPaymentRequestException.class,
                () -> paymentService.createPayment(validRequest)
        );

        assertEquals("UserId must not be empty", exception.getMessage());
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void createPayment_ShouldSetStatusToPending() {
        // Given
        Payment savedPayment = Payment.builder()
                .id(1L)
                .amount(BigDecimal.valueOf(validRequest.getAmount()))
                .currency(validRequest.getCurrency())
                .method(validRequest.getMethod().getValue())
                .status(PaymentStatus.PENDING)
                .userId(validRequest.getUserId())
                .orderId(validRequest.getOrderId() != null && validRequest.getOrderId().isPresent() ? validRequest.getOrderId().get() : null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            assertEquals(PaymentStatus.PENDING, payment.getStatus());
            payment.setId(1L);
            return payment;
        });

        // When
        PaymentResponse response = paymentService.createPayment(validRequest);

        // Then
        assertEquals(PaymentResponse.StatusEnum.PENDING, response.getStatus());
    }

    @Test
    void createPayment_WithSameOrderIdAndUserId_ShouldReturnExistingPayment() {
        // Given - existing PENDING payment
        Payment existingPayment = Payment.builder()
                .id(100L)
                .amount(BigDecimal.valueOf(99.99))
                .currency("USD")
                .method("CREDIT_CARD")
                .status(PaymentStatus.PENDING)
                .userId("user123")
                .orderId("order456")
                .createdAt(LocalDateTime.now().minusMinutes(5))
                .updatedAt(LocalDateTime.now().minusMinutes(5))
                .build();

        when(paymentRepository.findByOrderIdAndUserIdAndStatus(
                eq("order456"), eq("user123"), eq(PaymentStatus.PENDING)))
                .thenReturn(Optional.of(existingPayment));

        // When
        PaymentResponse response = paymentService.createPayment(validRequest);

        // Then
        assertNotNull(response);
        assertEquals(100L, response.getId()); // Existing payment ID
        assertEquals(99.99, response.getAmount());
        assertEquals("order456", response.getOrderId().get());
        assertEquals("user123", response.getUserId());
        assertEquals(PaymentResponse.StatusEnum.PENDING, response.getStatus());

        // Verify that save was NOT called (existing payment returned)
        verify(paymentRepository, never()).save(any(Payment.class));
        // Verify that idempotency check was performed
        verify(paymentRepository, times(1)).findByOrderIdAndUserIdAndStatus(
                eq("order456"), eq("user123"), eq(PaymentStatus.PENDING));
    }

    @Test
    void createPayment_WithoutOrderId_ShouldCreateNewPayment() {
        // Given - no orderId, so idempotency check should be skipped
        validRequest.setOrderId(JsonNullable.undefined());
        Payment savedPayment = Payment.builder()
                .id(1L)
                .amount(BigDecimal.valueOf(validRequest.getAmount()))
                .currency(validRequest.getCurrency())
                .method(validRequest.getMethod().getValue())
                .status(PaymentStatus.PENDING)
                .userId(validRequest.getUserId())
                .orderId(null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        // When
        PaymentResponse response = paymentService.createPayment(validRequest);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertFalse(response.getOrderId().isPresent());

        // Verify that idempotency check was NOT performed (orderId is null)
        verify(paymentRepository, never()).findByOrderIdAndUserIdAndStatus(any(), any(), any());
        // Verify that save was called
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void createPayment_WithDifferentOrderId_ShouldCreateNewPayment() {
        // Given - different orderId
        validRequest.setOrderId(JsonNullable.of("order999"));
        Payment savedPayment = Payment.builder()
                .id(2L)
                .amount(BigDecimal.valueOf(validRequest.getAmount()))
                .currency(validRequest.getCurrency())
                .method(validRequest.getMethod().getValue())
                .status(PaymentStatus.PENDING)
                .userId(validRequest.getUserId())
                .orderId("order999")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(paymentRepository.findByOrderIdAndUserIdAndStatus(
                eq("order999"), eq("user123"), eq(PaymentStatus.PENDING)))
                .thenReturn(Optional.empty()); // No existing payment
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        // When
        PaymentResponse response = paymentService.createPayment(validRequest);

        // Then
        assertNotNull(response);
        assertEquals(2L, response.getId());
        assertEquals("order999", response.getOrderId().get());

        // Verify that idempotency check was performed
        verify(paymentRepository, times(1)).findByOrderIdAndUserIdAndStatus(
                eq("order999"), eq("user123"), eq(PaymentStatus.PENDING));
        // Verify that save was called (new payment created)
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void createPayment_WithSameOrderIdButDifferentUserId_ShouldCreateNewPayment() {
        // Given - same orderId but different userId
        validRequest.setUserId("user999");
        Payment savedPayment = Payment.builder()
                .id(3L)
                .amount(BigDecimal.valueOf(validRequest.getAmount()))
                .currency(validRequest.getCurrency())
                .method(validRequest.getMethod().getValue())
                .status(PaymentStatus.PENDING)
                .userId("user999")
                .orderId("order456")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(paymentRepository.findByOrderIdAndUserIdAndStatus(
                eq("order456"), eq("user999"), eq(PaymentStatus.PENDING)))
                .thenReturn(Optional.empty()); // No existing payment for this userId
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        // When
        PaymentResponse response = paymentService.createPayment(validRequest);

        // Then
        assertNotNull(response);
        assertEquals(3L, response.getId());
        assertEquals("user999", response.getUserId());
        assertEquals("order456", response.getOrderId().get());

        // Verify that idempotency check was performed with correct userId
        verify(paymentRepository, times(1)).findByOrderIdAndUserIdAndStatus(
                eq("order456"), eq("user999"), eq(PaymentStatus.PENDING));
        // Verify that save was called (new payment created)
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void createPayment_WithSameOrderIdAndUserIdButDifferentStatus_ShouldCreateNewPayment() {
        // Given - existing payment with APPROVED status (not PENDING)
        when(paymentRepository.findByOrderIdAndUserIdAndStatus(
                eq("order456"), eq("user123"), eq(PaymentStatus.PENDING)))
                .thenReturn(Optional.empty()); // No PENDING payment found

        Payment savedPayment = Payment.builder()
                .id(4L)
                .amount(BigDecimal.valueOf(validRequest.getAmount()))
                .currency(validRequest.getCurrency())
                .method(validRequest.getMethod().getValue())
                .status(PaymentStatus.PENDING)
                .userId(validRequest.getUserId())
                .orderId("order456")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        // When
        PaymentResponse response = paymentService.createPayment(validRequest);

        // Then
        assertNotNull(response);
        assertEquals(4L, response.getId());

        // Verify that idempotency check was performed (only checks PENDING status)
        verify(paymentRepository, times(1)).findByOrderIdAndUserIdAndStatus(
                eq("order456"), eq("user123"), eq(PaymentStatus.PENDING));
        // Verify that save was called (new payment created because existing one is not PENDING)
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void getPaymentById_WithExistingId_ShouldReturnPayment() {
        // Given
        Long paymentId = 1L;
        Payment payment = Payment.builder()
                .id(paymentId)
                .amount(BigDecimal.valueOf(99.99))
                .currency("USD")
                .method("CREDIT_CARD")
                .status(PaymentStatus.PENDING)
                .userId("user123")
                .orderId("order456")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        // When
        PaymentResponse response = paymentService.getPaymentById(paymentId);

        // Then
        assertNotNull(response);
        assertEquals(paymentId, response.getId());
        assertEquals(99.99, response.getAmount());
        assertEquals("USD", response.getCurrency());
        assertEquals("CREDIT_CARD", response.getMethod());
        assertEquals(PaymentResponse.StatusEnum.PENDING, response.getStatus());
        assertEquals("user123", response.getUserId());
        assertTrue(response.getOrderId().isPresent());
        assertEquals("order456", response.getOrderId().get());
        assertNotNull(response.getCreatedAt());
        assertNotNull(response.getUpdatedAt());

        verify(paymentRepository, times(1)).findById(paymentId);
    }

    @Test
    void getPaymentById_WithNonExistentId_ShouldThrowException() {
        // Given
        Long paymentId = 999L;
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        // When/Then
        PaymentNotFoundException exception = assertThrows(
                PaymentNotFoundException.class,
                () -> paymentService.getPaymentById(paymentId)
        );

        assertEquals("Payment not found with ID: " + paymentId, exception.getMessage());
        verify(paymentRepository, times(1)).findById(paymentId);
    }

    // Status Transition Validation Tests

    @Test
    void validateStatusTransition_FromPendingToApproved_ShouldNotThrowException() {
        // Given
        Long paymentId = 1L;

        // When/Then - should not throw exception
        assertDoesNotThrow(() -> 
            paymentService.validateStatusTransition(paymentId, PaymentStatus.PENDING, PaymentStatus.APPROVED)
        );
    }

    @Test
    void validateStatusTransition_FromPendingToFailed_ShouldNotThrowException() {
        // Given
        Long paymentId = 1L;

        // When/Then - should not throw exception
        assertDoesNotThrow(() -> 
            paymentService.validateStatusTransition(paymentId, PaymentStatus.PENDING, PaymentStatus.FAILED)
        );
    }

    @Test
    void validateStatusTransition_FromPendingToCanceled_ShouldNotThrowException() {
        // Given
        Long paymentId = 1L;

        // When/Then - should not throw exception
        assertDoesNotThrow(() -> 
            paymentService.validateStatusTransition(paymentId, PaymentStatus.PENDING, PaymentStatus.CANCELED)
        );
    }

    @Test
    void validateStatusTransition_FromApprovedToPending_ShouldThrowException() {
        // Given
        Long paymentId = 1L;

        // When/Then
        InvalidStatusTransitionException exception = assertThrows(
                InvalidStatusTransitionException.class,
                () -> paymentService.validateStatusTransition(paymentId, PaymentStatus.APPROVED, PaymentStatus.PENDING)
        );

        assertTrue(exception.getMessage().contains("Cannot transition from terminal state"));
        assertTrue(exception.getMessage().contains("APPROVED"));
    }

    @Test
    void validateStatusTransition_FromFailedToApproved_ShouldThrowException() {
        // Given
        Long paymentId = 1L;

        // When/Then
        InvalidStatusTransitionException exception = assertThrows(
                InvalidStatusTransitionException.class,
                () -> paymentService.validateStatusTransition(paymentId, PaymentStatus.FAILED, PaymentStatus.APPROVED)
        );

        assertTrue(exception.getMessage().contains("Cannot transition from terminal state"));
        assertTrue(exception.getMessage().contains("FAILED"));
    }

    @Test
    void validateStatusTransition_FromCanceledToPending_ShouldThrowException() {
        // Given
        Long paymentId = 1L;

        // When/Then
        InvalidStatusTransitionException exception = assertThrows(
                InvalidStatusTransitionException.class,
                () -> paymentService.validateStatusTransition(paymentId, PaymentStatus.CANCELED, PaymentStatus.PENDING)
        );

        assertTrue(exception.getMessage().contains("Cannot transition from terminal state"));
        assertTrue(exception.getMessage().contains("CANCELED"));
    }

    @Test
    void validateStatusTransition_FromApprovedToFailed_ShouldThrowException() {
        // Given
        Long paymentId = 1L;

        // When/Then
        InvalidStatusTransitionException exception = assertThrows(
                InvalidStatusTransitionException.class,
                () -> paymentService.validateStatusTransition(paymentId, PaymentStatus.APPROVED, PaymentStatus.FAILED)
        );

        assertTrue(exception.getMessage().contains("Cannot transition from terminal state"));
        assertTrue(exception.getMessage().contains("APPROVED"));
    }

    @Test
    void validateStatusTransition_WithNullTargetStatus_ShouldThrowException() {
        // Given
        Long paymentId = 1L;

        // When/Then
        InvalidStatusTransitionException exception = assertThrows(
                InvalidStatusTransitionException.class,
                () -> paymentService.validateStatusTransition(paymentId, PaymentStatus.PENDING, null)
        );

        assertEquals("Target status cannot be null", exception.getMessage());
    }

    @Test
    void validateStatusTransition_FromPendingToPending_ShouldThrowException() {
        // Given
        Long paymentId = 1L;

        // When/Then
        InvalidStatusTransitionException exception = assertThrows(
                InvalidStatusTransitionException.class,
                () -> paymentService.validateStatusTransition(paymentId, PaymentStatus.PENDING, PaymentStatus.PENDING)
        );

        assertTrue(exception.getMessage().contains("Cannot transition from PENDING to PENDING (same status)"));
    }

    // updatePaymentStatus Method Tests

    @Test
    void updatePaymentStatus_FromPendingToApproved_ShouldUpdateSuccessfully() {
        // Given
        Long paymentId = 1L;
        Payment payment = Payment.builder()
                .id(paymentId)
                .amount(BigDecimal.valueOf(99.99))
                .currency("USD")
                .method("CREDIT_CARD")
                .status(PaymentStatus.PENDING)
                .userId("user123")
                .orderId("order456")
                .createdAt(LocalDateTime.now().minusMinutes(5))
                .updatedAt(LocalDateTime.now().minusMinutes(5))
                .build();

        Payment updatedPayment = Payment.builder()
                .id(paymentId)
                .amount(BigDecimal.valueOf(99.99))
                .currency("USD")
                .method("CREDIT_CARD")
                .status(PaymentStatus.APPROVED)
                .userId("user123")
                .orderId("order456")
                .createdAt(payment.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(updatedPayment);

        // When
        PaymentResponse response = paymentService.updatePaymentStatus(paymentId, PaymentStatus.APPROVED);

        // Then
        assertNotNull(response);
        assertEquals(paymentId, response.getId());
        assertEquals(PaymentResponse.StatusEnum.APPROVED, response.getStatus());
        verify(paymentRepository, times(1)).findById(paymentId);
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void updatePaymentStatus_FromPendingToFailed_ShouldUpdateSuccessfully() {
        // Given
        Long paymentId = 1L;
        Payment payment = Payment.builder()
                .id(paymentId)
                .amount(BigDecimal.valueOf(99.99))
                .currency("USD")
                .method("CREDIT_CARD")
                .status(PaymentStatus.PENDING)
                .userId("user123")
                .orderId("order456")
                .createdAt(LocalDateTime.now().minusMinutes(5))
                .updatedAt(LocalDateTime.now().minusMinutes(5))
                .build();

        Payment updatedPayment = Payment.builder()
                .id(paymentId)
                .amount(BigDecimal.valueOf(99.99))
                .currency("USD")
                .method("CREDIT_CARD")
                .status(PaymentStatus.FAILED)
                .userId("user123")
                .orderId("order456")
                .createdAt(payment.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(updatedPayment);

        // When
        PaymentResponse response = paymentService.updatePaymentStatus(paymentId, PaymentStatus.FAILED);

        // Then
        assertNotNull(response);
        assertEquals(paymentId, response.getId());
        assertEquals(PaymentResponse.StatusEnum.FAILED, response.getStatus());
        verify(paymentRepository, times(1)).findById(paymentId);
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void updatePaymentStatus_FromPendingToCanceled_ShouldUpdateSuccessfully() {
        // Given
        Long paymentId = 1L;
        Payment payment = Payment.builder()
                .id(paymentId)
                .amount(BigDecimal.valueOf(99.99))
                .currency("USD")
                .method("CREDIT_CARD")
                .status(PaymentStatus.PENDING)
                .userId("user123")
                .orderId("order456")
                .createdAt(LocalDateTime.now().minusMinutes(5))
                .updatedAt(LocalDateTime.now().minusMinutes(5))
                .build();

        Payment updatedPayment = Payment.builder()
                .id(paymentId)
                .amount(BigDecimal.valueOf(99.99))
                .currency("USD")
                .method("CREDIT_CARD")
                .status(PaymentStatus.CANCELED)
                .userId("user123")
                .orderId("order456")
                .createdAt(payment.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(updatedPayment);

        // When
        PaymentResponse response = paymentService.updatePaymentStatus(paymentId, PaymentStatus.CANCELED);

        // Then
        assertNotNull(response);
        assertEquals(paymentId, response.getId());
        assertEquals(PaymentResponse.StatusEnum.CANCELED, response.getStatus());
        verify(paymentRepository, times(1)).findById(paymentId);
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void updatePaymentStatus_WithNonExistentPayment_ShouldThrowException() {
        // Given
        Long paymentId = 999L;
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        // When/Then
        PaymentNotFoundException exception = assertThrows(
                PaymentNotFoundException.class,
                () -> paymentService.updatePaymentStatus(paymentId, PaymentStatus.APPROVED)
        );

        assertEquals("Payment not found with ID: " + paymentId, exception.getMessage());
        verify(paymentRepository, times(1)).findById(paymentId);
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void updatePaymentStatus_FromApprovedToPending_ShouldThrowException() {
        // Given
        Long paymentId = 1L;
        Payment payment = Payment.builder()
                .id(paymentId)
                .amount(BigDecimal.valueOf(99.99))
                .currency("USD")
                .method("CREDIT_CARD")
                .status(PaymentStatus.APPROVED)
                .userId("user123")
                .orderId("order456")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        // When/Then
        InvalidStatusTransitionException exception = assertThrows(
                InvalidStatusTransitionException.class,
                () -> paymentService.updatePaymentStatus(paymentId, PaymentStatus.PENDING)
        );

        assertTrue(exception.getMessage().contains("Cannot transition from terminal state"));
        assertTrue(exception.getMessage().contains("APPROVED"));
        verify(paymentRepository, times(1)).findById(paymentId);
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void updatePaymentStatus_FromFailedToApproved_ShouldThrowException() {
        // Given
        Long paymentId = 1L;
        Payment payment = Payment.builder()
                .id(paymentId)
                .amount(BigDecimal.valueOf(99.99))
                .currency("USD")
                .method("CREDIT_CARD")
                .status(PaymentStatus.FAILED)
                .userId("user123")
                .orderId("order456")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        // When/Then
        InvalidStatusTransitionException exception = assertThrows(
                InvalidStatusTransitionException.class,
                () -> paymentService.updatePaymentStatus(paymentId, PaymentStatus.APPROVED)
        );

        assertTrue(exception.getMessage().contains("Cannot transition from terminal state"));
        assertTrue(exception.getMessage().contains("FAILED"));
        verify(paymentRepository, times(1)).findById(paymentId);
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void updatePaymentStatus_FromCanceledToApproved_ShouldThrowException() {
        // Given
        Long paymentId = 1L;
        Payment payment = Payment.builder()
                .id(paymentId)
                .amount(BigDecimal.valueOf(99.99))
                .currency("USD")
                .method("CREDIT_CARD")
                .status(PaymentStatus.CANCELED)
                .userId("user123")
                .orderId("order456")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        // When/Then
        InvalidStatusTransitionException exception = assertThrows(
                InvalidStatusTransitionException.class,
                () -> paymentService.updatePaymentStatus(paymentId, PaymentStatus.APPROVED)
        );

        assertTrue(exception.getMessage().contains("Cannot transition from terminal state"));
        assertTrue(exception.getMessage().contains("CANCELED"));
        verify(paymentRepository, times(1)).findById(paymentId);
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void updatePaymentStatus_ShouldUpdateUpdatedAtTimestamp() {
        // Given
        Long paymentId = 1L;
        LocalDateTime originalUpdatedAt = LocalDateTime.now().minusMinutes(5);
        Payment payment = Payment.builder()
                .id(paymentId)
                .amount(BigDecimal.valueOf(99.99))
                .currency("USD")
                .method("CREDIT_CARD")
                .status(PaymentStatus.PENDING)
                .userId("user123")
                .orderId("order456")
                .createdAt(LocalDateTime.now().minusMinutes(10))
                .updatedAt(originalUpdatedAt)
                .build();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment savedPayment = invocation.getArgument(0);
            // Verify updatedAt was changed
            assertTrue(savedPayment.getUpdatedAt().isAfter(originalUpdatedAt));
            return savedPayment;
        });

        // When
        paymentService.updatePaymentStatus(paymentId, PaymentStatus.APPROVED);

        // Then
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void listPayments_WithNoFilters_ShouldReturnAllPayments() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        Payment payment1 = Payment.builder()
                .id(1L)
                .amount(BigDecimal.valueOf(99.99))
                .currency("USD")
                .method("CREDIT_CARD")
                .status(PaymentStatus.PENDING)
                .userId("user123")
                .orderId("order456")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        Payment payment2 = Payment.builder()
                .id(2L)
                .amount(BigDecimal.valueOf(150.00))
                .currency("EUR")
                .method("PAYPAL")
                .status(PaymentStatus.APPROVED)
                .userId("user456")
                .orderId("order789")
                .createdAt(LocalDateTime.now().minusHours(1))
                .updatedAt(LocalDateTime.now().minusHours(1))
                .build();
        Page<Payment> paymentPage = new PageImpl<>(List.of(payment1, payment2), pageable, 2);

        when(paymentRepository.findAllOrderByCreatedAtDesc(pageable)).thenReturn(paymentPage);

        // When
        PaymentListResponse response = paymentService.listPayments(null, null, null, null, null);

        // Then
        assertNotNull(response);
        assertEquals(2, response.getContent().size());
        assertEquals(2, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
        assertEquals(0, response.getPage());
        assertEquals(20, response.getSize());
        verify(paymentRepository, times(1)).findAllOrderByCreatedAtDesc(pageable);
    }

    @Test
    void listPayments_WithStatusFilter_ShouldReturnFilteredPayments() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        Payment payment = Payment.builder()
                .id(1L)
                .amount(BigDecimal.valueOf(99.99))
                .currency("USD")
                .method("CREDIT_CARD")
                .status(PaymentStatus.PENDING)
                .userId("user123")
                .orderId("order456")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        Page<Payment> paymentPage = new PageImpl<>(List.of(payment), pageable, 1);

        when(paymentRepository.findByStatusOrderByCreatedAtDesc(PaymentStatus.PENDING, pageable)).thenReturn(paymentPage);

        // When
        PaymentListResponse response = paymentService.listPayments("PENDING", null, null, null, null);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals(1, response.getTotalElements());
        assertEquals(PaymentResponse.StatusEnum.PENDING, response.getContent().get(0).getStatus());
        verify(paymentRepository, times(1)).findByStatusOrderByCreatedAtDesc(PaymentStatus.PENDING, pageable);
    }

    @Test
    void listPayments_WithUserIdFilter_ShouldReturnFilteredPayments() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        Payment payment = Payment.builder()
                .id(1L)
                .amount(BigDecimal.valueOf(99.99))
                .currency("USD")
                .method("CREDIT_CARD")
                .status(PaymentStatus.PENDING)
                .userId("user123")
                .orderId("order456")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        Page<Payment> paymentPage = new PageImpl<>(List.of(payment), pageable, 1);

        when(paymentRepository.findByUserIdOrderByCreatedAtDesc("user123", pageable)).thenReturn(paymentPage);

        // When
        PaymentListResponse response = paymentService.listPayments(null, "user123", null, null, null);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals("user123", response.getContent().get(0).getUserId());
        verify(paymentRepository, times(1)).findByUserIdOrderByCreatedAtDesc("user123", pageable);
    }

    @Test
    void listPayments_WithOrderIdFilter_ShouldReturnFilteredPayments() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        Payment payment = Payment.builder()
                .id(1L)
                .amount(BigDecimal.valueOf(99.99))
                .currency("USD")
                .method("CREDIT_CARD")
                .status(PaymentStatus.PENDING)
                .userId("user123")
                .orderId("order456")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        Page<Payment> paymentPage = new PageImpl<>(List.of(payment), pageable, 1);

        when(paymentRepository.findByOrderIdOrderByCreatedAtDesc("order456", pageable)).thenReturn(paymentPage);

        // When
        PaymentListResponse response = paymentService.listPayments(null, null, "order456", null, null);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertTrue(response.getContent().get(0).getOrderId().isPresent());
        assertEquals("order456", response.getContent().get(0).getOrderId().get());
        verify(paymentRepository, times(1)).findByOrderIdOrderByCreatedAtDesc("order456", pageable);
    }

    @Test
    void listPayments_WithMultipleFilters_ShouldReturnFilteredPayments() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        Payment payment = Payment.builder()
                .id(1L)
                .amount(BigDecimal.valueOf(99.99))
                .currency("USD")
                .method("CREDIT_CARD")
                .status(PaymentStatus.PENDING)
                .userId("user123")
                .orderId("order456")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        Page<Payment> paymentPage = new PageImpl<>(List.of(payment), pageable, 1);

        when(paymentRepository.findByStatusAndUserIdOrderByCreatedAtDesc(PaymentStatus.PENDING, "user123", pageable)).thenReturn(paymentPage);

        // When
        PaymentListResponse response = paymentService.listPayments("PENDING", "user123", null, null, null);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals(PaymentResponse.StatusEnum.PENDING, response.getContent().get(0).getStatus());
        assertEquals("user123", response.getContent().get(0).getUserId());
        verify(paymentRepository, times(1)).findByStatusAndUserIdOrderByCreatedAtDesc(PaymentStatus.PENDING, "user123", pageable);
    }

    @Test
    void listPayments_WithPagination_ShouldReturnPaginatedResults() {
        // Given
        Pageable pageable = PageRequest.of(1, 5);
        List<Payment> payments = List.of(
                Payment.builder().id(6L).amount(BigDecimal.valueOf(10.0)).currency("USD").method("CREDIT_CARD")
                        .status(PaymentStatus.PENDING).userId("user1").createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now()).build(),
                Payment.builder().id(7L).amount(BigDecimal.valueOf(20.0)).currency("EUR").method("PAYPAL")
                        .status(PaymentStatus.APPROVED).userId("user2").createdAt(LocalDateTime.now().minusHours(1))
                        .updatedAt(LocalDateTime.now().minusHours(1)).build()
        );
        Page<Payment> paymentPage = new PageImpl<>(payments, pageable, 10);

        when(paymentRepository.findAllOrderByCreatedAtDesc(pageable)).thenReturn(paymentPage);

        // When
        PaymentListResponse response = paymentService.listPayments(null, null, null, 1, 5);

        // Then
        assertNotNull(response);
        assertEquals(2, response.getContent().size());
        assertEquals(10, response.getTotalElements());
        assertEquals(2, response.getTotalPages());
        assertEquals(1, response.getPage());
        assertEquals(5, response.getSize());
        verify(paymentRepository, times(1)).findAllOrderByCreatedAtDesc(pageable);
    }

    @Test
    void listPayments_WithInvalidPage_ShouldThrowException() {
        // When/Then
        InvalidPaymentRequestException exception = assertThrows(
                InvalidPaymentRequestException.class,
                () -> paymentService.listPayments(null, null, null, -1, null)
        );

        assertEquals("Page number must be >= 0", exception.getMessage());
    }

    @Test
    void listPayments_WithInvalidSize_ShouldThrowException() {
        // When/Then
        InvalidPaymentRequestException exception = assertThrows(
                InvalidPaymentRequestException.class,
                () -> paymentService.listPayments(null, null, null, null, 200)
        );

        assertEquals("Page size must be between 1 and 100", exception.getMessage());
    }

    @Test
    void listPayments_WithInvalidStatus_ShouldThrowException() {
        // When/Then
        InvalidPaymentRequestException exception = assertThrows(
                InvalidPaymentRequestException.class,
                () -> paymentService.listPayments("INVALID", null, null, null, null)
        );

        assertTrue(exception.getMessage().contains("Invalid status value"));
    }
}
