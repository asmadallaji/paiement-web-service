package com.asma.paymentservice.service;

import com.asma.paymentservice.entity.Invoice;
import com.asma.paymentservice.entity.InvoiceStatus;
import com.asma.paymentservice.entity.Payment;
import com.asma.paymentservice.entity.PaymentStatus;
import com.asma.paymentservice.exception.InvalidInvoiceRequestException;
import com.asma.paymentservice.exception.InvalidStatusTransitionException;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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

    @Test
    void updateInvoiceStatus_FromCreatedToSent_ShouldUpdateStatusAndSetSentAt() {
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
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Invoice updated = invoiceService.updateInvoiceStatus(1L, InvoiceStatus.SENT);

        // Then
        assertNotNull(updated);
        assertEquals(InvoiceStatus.SENT, updated.getStatus());
        assertNotNull(updated.getSentAt());
        assertEquals(LocalDate.now(), updated.getSentAt());
        verify(invoiceRepository, times(1)).findById(1L);
        verify(invoiceRepository, times(1)).save(any(Invoice.class));
    }

    @Test
    void updateInvoiceStatus_FromCreatedToPaid_ShouldUpdateStatusAndSetPaidAt() {
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
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Invoice updated = invoiceService.updateInvoiceStatus(1L, InvoiceStatus.PAID);

        // Then
        assertNotNull(updated);
        assertEquals(InvoiceStatus.PAID, updated.getStatus());
        assertNotNull(updated.getPaidAt());
        assertEquals(LocalDate.now(), updated.getPaidAt());
        verify(invoiceRepository, times(1)).findById(1L);
        verify(invoiceRepository, times(1)).save(any(Invoice.class));
    }

    @Test
    void updateInvoiceStatus_FromCreatedToCancelled_ShouldUpdateStatusAndSetCancelledAt() {
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
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Invoice updated = invoiceService.updateInvoiceStatus(1L, InvoiceStatus.CANCELLED);

        // Then
        assertNotNull(updated);
        assertEquals(InvoiceStatus.CANCELLED, updated.getStatus());
        assertNotNull(updated.getCancelledAt());
        assertEquals(LocalDate.now(), updated.getCancelledAt());
        verify(invoiceRepository, times(1)).findById(1L);
        verify(invoiceRepository, times(1)).save(any(Invoice.class));
    }

    @Test
    void updateInvoiceStatus_FromSentToPaid_ShouldUpdateStatusAndSetPaidAt() {
        // Given
        Invoice invoice = Invoice.builder()
                .id(1L)
                .invoiceNumber("INV-001")
                .paymentId(1L)
                .userId("user123")
                .amount(BigDecimal.valueOf(99.99))
                .currency("USD")
                .status(InvoiceStatus.SENT)
                .issueDate(LocalDate.now())
                .sentAt(LocalDate.now().minusDays(1))
                .build();

        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Invoice updated = invoiceService.updateInvoiceStatus(1L, InvoiceStatus.PAID);

        // Then
        assertNotNull(updated);
        assertEquals(InvoiceStatus.PAID, updated.getStatus());
        assertNotNull(updated.getPaidAt());
        assertEquals(LocalDate.now(), updated.getPaidAt());
        verify(invoiceRepository, times(1)).findById(1L);
        verify(invoiceRepository, times(1)).save(any(Invoice.class));
    }

    @Test
    void updateInvoiceStatus_WithNonExistentInvoice_ShouldThrowException() {
        // Given
        when(invoiceRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(InvoiceNotFoundException.class, () -> invoiceService.updateInvoiceStatus(1L, InvoiceStatus.SENT));
        verify(invoiceRepository, times(1)).findById(1L);
        verify(invoiceRepository, never()).save(any(Invoice.class));
    }

    @Test
    void validateStatusTransition_FromCreatedToSent_ShouldAllow() {
        // When & Then - should not throw
        assertDoesNotThrow(() -> invoiceService.validateStatusTransition(1L, InvoiceStatus.CREATED, InvoiceStatus.SENT));
    }

    @Test
    void validateStatusTransition_FromCreatedToPaid_ShouldAllow() {
        // When & Then - should not throw
        assertDoesNotThrow(() -> invoiceService.validateStatusTransition(1L, InvoiceStatus.CREATED, InvoiceStatus.PAID));
    }

    @Test
    void validateStatusTransition_FromCreatedToCancelled_ShouldAllow() {
        // When & Then - should not throw
        assertDoesNotThrow(() -> invoiceService.validateStatusTransition(1L, InvoiceStatus.CREATED, InvoiceStatus.CANCELLED));
    }

    @Test
    void validateStatusTransition_FromSentToPaid_ShouldAllow() {
        // When & Then - should not throw
        assertDoesNotThrow(() -> invoiceService.validateStatusTransition(1L, InvoiceStatus.SENT, InvoiceStatus.PAID));
    }

    @Test
    void validateStatusTransition_FromSentToCreated_ShouldReject() {
        // When & Then
        assertThrows(InvalidStatusTransitionException.class, 
                () -> invoiceService.validateStatusTransition(1L, InvoiceStatus.SENT, InvoiceStatus.CREATED));
    }

    @Test
    void validateStatusTransition_FromPaidToSent_ShouldReject() {
        // When & Then
        assertThrows(InvalidStatusTransitionException.class, 
                () -> invoiceService.validateStatusTransition(1L, InvoiceStatus.PAID, InvoiceStatus.SENT));
    }

    @Test
    void validateStatusTransition_FromPaidToAny_ShouldReject() {
        // When & Then
        assertThrows(InvalidStatusTransitionException.class, 
                () -> invoiceService.validateStatusTransition(1L, InvoiceStatus.PAID, InvoiceStatus.CREATED));
        assertThrows(InvalidStatusTransitionException.class, 
                () -> invoiceService.validateStatusTransition(1L, InvoiceStatus.PAID, InvoiceStatus.CANCELLED));
    }

    @Test
    void validateStatusTransition_FromCancelledToAny_ShouldReject() {
        // When & Then
        assertThrows(InvalidStatusTransitionException.class, 
                () -> invoiceService.validateStatusTransition(1L, InvoiceStatus.CANCELLED, InvoiceStatus.CREATED));
        assertThrows(InvalidStatusTransitionException.class, 
                () -> invoiceService.validateStatusTransition(1L, InvoiceStatus.CANCELLED, InvoiceStatus.PAID));
    }

    @Test
    void validateStatusTransition_SameStatus_ShouldReject() {
        // When & Then
        assertThrows(InvalidStatusTransitionException.class, 
                () -> invoiceService.validateStatusTransition(1L, InvoiceStatus.CREATED, InvoiceStatus.CREATED));
    }

    @Test
    void validateStatusTransition_NullTargetStatus_ShouldReject() {
        // When & Then
        assertThrows(InvalidStatusTransitionException.class, 
                () -> invoiceService.validateStatusTransition(1L, InvoiceStatus.CREATED, null));
    }

    // Unit tests for listInvoices
    @Test
    void listInvoices_WithNoFilters_ShouldReturnAllInvoices() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        Invoice invoice1 = Invoice.builder()
                .id(1L)
                .invoiceNumber("INV-001")
                .paymentId(1L)
                .userId("user123")
                .amount(BigDecimal.valueOf(99.99))
                .currency("USD")
                .status(InvoiceStatus.CREATED)
                .issueDate(LocalDate.now())
                .build();
        Invoice invoice2 = Invoice.builder()
                .id(2L)
                .invoiceNumber("INV-002")
                .paymentId(2L)
                .userId("user456")
                .amount(BigDecimal.valueOf(150.00))
                .currency("EUR")
                .status(InvoiceStatus.SENT)
                .issueDate(LocalDate.now().minusDays(1))
                .build();
        Page<Invoice> invoicePage = new PageImpl<>(List.of(invoice1, invoice2), pageable, 2);

        when(invoiceRepository.findAllOrderByIssueDateDesc(pageable)).thenReturn(invoicePage);

        // When
        com.asma.paymentservice.dto.InvoiceListResponse response = invoiceService.listInvoices(null, null, null, null, null, null);

        // Then
        assertNotNull(response);
        assertEquals(2, response.getContent().size());
        assertEquals(2, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
        assertEquals(0, response.getPage());
        assertEquals(20, response.getSize());
        verify(invoiceRepository, times(1)).findAllOrderByIssueDateDesc(pageable);
    }

    @Test
    void listInvoices_WithStatusFilter_ShouldReturnFilteredInvoices() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
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
        Page<Invoice> invoicePage = new PageImpl<>(List.of(invoice), pageable, 1);

        when(invoiceRepository.findByStatusOrderByIssueDateDesc(InvoiceStatus.CREATED, pageable)).thenReturn(invoicePage);

        // When
        com.asma.paymentservice.dto.InvoiceListResponse response = invoiceService.listInvoices("CREATED", null, null, null, null, null);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals(1, response.getTotalElements());
        assertEquals(com.asma.paymentservice.dto.InvoiceResponse.StatusEnum.CREATED, response.getContent().get(0).getStatus());
        verify(invoiceRepository, times(1)).findByStatusOrderByIssueDateDesc(InvoiceStatus.CREATED, pageable);
    }

    @Test
    void listInvoices_WithUserIdFilter_ShouldReturnFilteredInvoices() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
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
        Page<Invoice> invoicePage = new PageImpl<>(List.of(invoice), pageable, 1);

        when(invoiceRepository.findByUserIdOrderByIssueDateDesc("user123", pageable)).thenReturn(invoicePage);

        // When
        com.asma.paymentservice.dto.InvoiceListResponse response = invoiceService.listInvoices(null, "user123", null, null, null, null);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals("user123", response.getContent().get(0).getUserId());
        verify(invoiceRepository, times(1)).findByUserIdOrderByIssueDateDesc("user123", pageable);
    }

    @Test
    void listInvoices_WithDateRangeFilter_ShouldReturnFilteredInvoices() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        LocalDate fromDate = LocalDate.now().minusDays(7);
        LocalDate toDate = LocalDate.now();
        Invoice invoice = Invoice.builder()
                .id(1L)
                .invoiceNumber("INV-001")
                .paymentId(1L)
                .userId("user123")
                .amount(BigDecimal.valueOf(99.99))
                .currency("USD")
                .status(InvoiceStatus.CREATED)
                .issueDate(LocalDate.now().minusDays(3))
                .build();
        Page<Invoice> invoicePage = new PageImpl<>(List.of(invoice), pageable, 1);

        when(invoiceRepository.findByIssueDateBetweenOrderByIssueDateDesc(fromDate, toDate, pageable)).thenReturn(invoicePage);

        // When
        com.asma.paymentservice.dto.InvoiceListResponse response = invoiceService.listInvoices(null, null, fromDate, toDate, null, null);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        verify(invoiceRepository, times(1)).findByIssueDateBetweenOrderByIssueDateDesc(fromDate, toDate, pageable);
    }

    @Test
    void listInvoices_WithMultipleFilters_ShouldReturnFilteredInvoices() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
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
        Page<Invoice> invoicePage = new PageImpl<>(List.of(invoice), pageable, 1);

        when(invoiceRepository.findByStatusAndUserIdOrderByIssueDateDesc(InvoiceStatus.CREATED, "user123", pageable)).thenReturn(invoicePage);

        // When
        com.asma.paymentservice.dto.InvoiceListResponse response = invoiceService.listInvoices("CREATED", "user123", null, null, null, null);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals(com.asma.paymentservice.dto.InvoiceResponse.StatusEnum.CREATED, response.getContent().get(0).getStatus());
        assertEquals("user123", response.getContent().get(0).getUserId());
        verify(invoiceRepository, times(1)).findByStatusAndUserIdOrderByIssueDateDesc(InvoiceStatus.CREATED, "user123", pageable);
    }

    @Test
    void listInvoices_WithPagination_ShouldReturnPaginatedResults() {
        // Given
        Pageable pageable = PageRequest.of(1, 5);
        List<Invoice> invoices = List.of(
                Invoice.builder().id(6L).invoiceNumber("INV-006").paymentId(6L).userId("user1")
                        .amount(BigDecimal.valueOf(10.0)).currency("USD").status(InvoiceStatus.CREATED)
                        .issueDate(LocalDate.now()).build(),
                Invoice.builder().id(7L).invoiceNumber("INV-007").paymentId(7L).userId("user2")
                        .amount(BigDecimal.valueOf(20.0)).currency("EUR").status(InvoiceStatus.SENT)
                        .issueDate(LocalDate.now().minusDays(1)).build()
        );
        Page<Invoice> invoicePage = new PageImpl<>(invoices, pageable, 10);

        when(invoiceRepository.findAllOrderByIssueDateDesc(pageable)).thenReturn(invoicePage);

        // When
        com.asma.paymentservice.dto.InvoiceListResponse response = invoiceService.listInvoices(null, null, null, null, 1, 5);

        // Then
        assertNotNull(response);
        assertEquals(2, response.getContent().size());
        assertEquals(10, response.getTotalElements());
        assertEquals(2, response.getTotalPages());
        assertEquals(1, response.getPage());
        assertEquals(5, response.getSize());
        verify(invoiceRepository, times(1)).findAllOrderByIssueDateDesc(pageable);
    }

    @Test
    void listInvoices_WithInvalidPage_ShouldThrowException() {
        // When/Then
        InvalidInvoiceRequestException exception = assertThrows(
                InvalidInvoiceRequestException.class,
                () -> invoiceService.listInvoices(null, null, null, null, -1, null)
        );

        assertEquals("Page number must be >= 0", exception.getMessage());
    }

    @Test
    void listInvoices_WithInvalidSize_ShouldThrowException() {
        // When/Then
        InvalidInvoiceRequestException exception = assertThrows(
                InvalidInvoiceRequestException.class,
                () -> invoiceService.listInvoices(null, null, null, null, null, 200)
        );

        assertEquals("Page size must be between 1 and 100", exception.getMessage());
    }

    @Test
    void listInvoices_WithInvalidDateRange_ShouldThrowException() {
        // Given
        LocalDate fromDate = LocalDate.now();
        LocalDate toDate = LocalDate.now().minusDays(1);

        // When/Then
        InvalidInvoiceRequestException exception = assertThrows(
                InvalidInvoiceRequestException.class,
                () -> invoiceService.listInvoices(null, null, fromDate, toDate, null, null)
        );

        assertEquals("fromDate must be <= toDate", exception.getMessage());
    }

    @Test
    void listInvoices_WithInvalidStatus_ShouldThrowException() {
        // When/Then
        InvalidInvoiceRequestException exception = assertThrows(
                InvalidInvoiceRequestException.class,
                () -> invoiceService.listInvoices("INVALID", null, null, null, null, null)
        );

        assertTrue(exception.getMessage().contains("Invalid status value"));
    }

    @Test
    void listInvoices_WithEmptyResultSet_ShouldReturnEmptyList() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        Page<Invoice> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(invoiceRepository.findAllOrderByIssueDateDesc(pageable)).thenReturn(emptyPage);

        // When
        com.asma.paymentservice.dto.InvoiceListResponse response = invoiceService.listInvoices(null, null, null, null, null, null);

        // Then
        assertNotNull(response);
        assertEquals(0, response.getContent().size());
        assertEquals(0, response.getTotalElements());
        assertEquals(0, response.getTotalPages());
        verify(invoiceRepository, times(1)).findAllOrderByIssueDateDesc(pageable);
    }
}

