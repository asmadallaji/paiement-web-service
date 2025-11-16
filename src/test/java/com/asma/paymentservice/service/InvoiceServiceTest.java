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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private InvoiceService invoiceService;

    private Payment approvedPayment;
    private Payment pendingPayment;

    @BeforeEach
    void setUp() {
        approvedPayment = Payment.builder()
                .id(1L)
                .amount(BigDecimal.valueOf(99.99))
                .currency("USD")
                .method("CREDIT_CARD")
                .status(PaymentStatus.APPROVED)
                .userId("user123")
                .orderId("order456")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        pendingPayment = Payment.builder()
                .id(2L)
                .amount(BigDecimal.valueOf(50.00))
                .currency("EUR")
                .method("DEBIT_CARD")
                .status(PaymentStatus.PENDING)
                .userId("user456")
                .orderId(null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createInvoiceFromPayment_WithValidPayment_ShouldCreateInvoice() {
        // Given
        when(invoiceRepository.existsByPaymentId(1L)).thenReturn(false);
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> {
            Invoice invoice = invocation.getArgument(0);
            invoice.setId(1L);
            return invoice;
        });

        // When
        Invoice invoice = invoiceService.createInvoiceFromPayment(approvedPayment);

        // Then
        assertNotNull(invoice);
        assertEquals(1L, invoice.getId());
        assertEquals(1L, invoice.getPaymentId());
        assertEquals("user123", invoice.getUserId());
        assertEquals(BigDecimal.valueOf(99.99), invoice.getAmount());
        assertEquals("USD", invoice.getCurrency());
        assertEquals(InvoiceStatus.CREATED, invoice.getStatus());
        assertEquals("order456", invoice.getOrderId());
        assertEquals(LocalDate.now(), invoice.getIssueDate());
        assertNotNull(invoice.getInvoiceNumber());
        assertTrue(invoice.getInvoiceNumber().startsWith("INV-"));

        verify(invoiceRepository, times(1)).existsByPaymentId(1L);
        verify(invoiceRepository, times(1)).save(any(Invoice.class));
    }

    @Test
    void createInvoiceFromPayment_WithDuplicatePayment_ShouldReturnExistingInvoice() {
        // Given
        Invoice existingInvoice = Invoice.builder()
                .id(1L)
                .invoiceNumber("INV-EXISTING")
                .paymentId(1L)
                .userId("user123")
                .amount(BigDecimal.valueOf(99.99))
                .currency("USD")
                .status(InvoiceStatus.CREATED)
                .issueDate(LocalDate.now())
                .orderId("order456")
                .build();

        when(invoiceRepository.existsByPaymentId(1L)).thenReturn(true);
        when(invoiceRepository.findByPaymentId(1L)).thenReturn(Optional.of(existingInvoice));

        // When
        Invoice invoice = invoiceService.createInvoiceFromPayment(approvedPayment);

        // Then
        assertNotNull(invoice);
        assertEquals(1L, invoice.getId());
        assertEquals("INV-EXISTING", invoice.getInvoiceNumber());
        verify(invoiceRepository, times(1)).existsByPaymentId(1L);
        verify(invoiceRepository, never()).save(any(Invoice.class));
    }

    @Test
    void getInvoiceById_WithExistingId_ShouldReturnInvoice() {
        // Given
        Invoice invoice = Invoice.builder()
                .id(1L)
                .invoiceNumber("INV-001")
                .paymentId(1L)
                .userId("user123")
                .amount(BigDecimal.valueOf(99.99))
                .currency("USD")
                .status(InvoiceStatus.CREATED)
                .issueDate(LocalDate.now())
                .build();

        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));

        // When
        Invoice result = invoiceService.getInvoiceById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("INV-001", result.getInvoiceNumber());
        verify(invoiceRepository, times(1)).findById(1L);
    }

    @Test
    void getInvoiceById_WithNonExistingId_ShouldThrowException() {
        // Given
        when(invoiceRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(InvoiceNotFoundException.class, () -> invoiceService.getInvoiceById(1L));
        verify(invoiceRepository, times(1)).findById(1L);
    }

    @Test
    void getInvoiceByPaymentId_WithExistingPaymentId_ShouldReturnInvoice() {
        // Given
        Invoice invoice = Invoice.builder()
                .id(1L)
                .invoiceNumber("INV-001")
                .paymentId(1L)
                .userId("user123")
                .amount(BigDecimal.valueOf(99.99))
                .currency("USD")
                .status(InvoiceStatus.CREATED)
                .issueDate(LocalDate.now())
                .build();

        when(invoiceRepository.findByPaymentId(1L)).thenReturn(Optional.of(invoice));

        // When
        Invoice result = invoiceService.getInvoiceByPaymentId(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1L, result.getPaymentId());
        verify(invoiceRepository, times(1)).findByPaymentId(1L);
    }

    @Test
    void getInvoiceByPaymentId_WithNonExistingPaymentId_ShouldThrowException() {
        // Given
        when(invoiceRepository.findByPaymentId(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(InvoiceNotFoundException.class, () -> invoiceService.getInvoiceByPaymentId(1L));
        verify(invoiceRepository, times(1)).findByPaymentId(1L);
    }

    @Test
    void createInvoiceManually_WithApprovedPayment_ShouldCreateInvoice() {
        // Given
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(approvedPayment));
        when(invoiceRepository.existsByPaymentId(1L)).thenReturn(false);
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> {
            Invoice invoice = invocation.getArgument(0);
            invoice.setId(1L);
            return invoice;
        });

        // When
        Invoice invoice = invoiceService.createInvoiceManually(1L);

        // Then
        assertNotNull(invoice);
        assertEquals(1L, invoice.getId());
        assertEquals(1L, invoice.getPaymentId());
        verify(paymentRepository, times(1)).findById(1L);
        verify(invoiceRepository, times(1)).existsByPaymentId(1L);
        verify(invoiceRepository, times(1)).save(any(Invoice.class));
    }

    @Test
    void createInvoiceManually_WithNonApprovedPayment_ShouldThrowException() {
        // Given
        when(paymentRepository.findById(2L)).thenReturn(Optional.of(pendingPayment));

        // When & Then
        assertThrows(InvalidInvoiceRequestException.class, () -> invoiceService.createInvoiceManually(2L));
        verify(paymentRepository, times(1)).findById(2L);
        verify(invoiceRepository, never()).save(any(Invoice.class));
    }

    @Test
    void createInvoiceManually_WithNonExistingPayment_ShouldThrowException() {
        // Given
        when(paymentRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(PaymentNotFoundException.class, () -> invoiceService.createInvoiceManually(999L));
        verify(paymentRepository, times(1)).findById(999L);
        verify(invoiceRepository, never()).save(any(Invoice.class));
    }

    @Test
    void createInvoiceManually_WithDuplicateInvoice_ShouldThrowException() {
        // Given
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(approvedPayment));
        when(invoiceRepository.existsByPaymentId(1L)).thenReturn(true);

        // When & Then
        assertThrows(InvalidInvoiceRequestException.class, () -> invoiceService.createInvoiceManually(1L));
        verify(paymentRepository, times(1)).findById(1L);
        verify(invoiceRepository, times(1)).existsByPaymentId(1L);
        verify(invoiceRepository, never()).save(any(Invoice.class));
    }
}

