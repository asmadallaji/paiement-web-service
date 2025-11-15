package com.asma.paymentservice.service;

import com.asma.paymentservice.dto.CreatePaymentRequest;
import org.openapitools.jackson.nullable.JsonNullable;
import com.asma.paymentservice.dto.PaymentResponse;
import com.asma.paymentservice.entity.Payment;
import com.asma.paymentservice.entity.PaymentStatus;
import com.asma.paymentservice.exception.InvalidPaymentRequestException;
import com.asma.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
}
