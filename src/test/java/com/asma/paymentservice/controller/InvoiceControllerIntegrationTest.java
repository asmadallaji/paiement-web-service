package com.asma.paymentservice.controller;

import com.asma.paymentservice.dto.CreateInvoiceRequest;
import com.asma.paymentservice.dto.UpdateInvoiceStatusRequest;
import com.asma.paymentservice.entity.Invoice;
import com.asma.paymentservice.entity.InvoiceStatus;
import com.asma.paymentservice.entity.Payment;
import com.asma.paymentservice.entity.PaymentStatus;
import com.asma.paymentservice.repository.InvoiceRepository;
import com.asma.paymentservice.repository.PaymentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class InvoiceControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @BeforeEach
    void setUp() {
        invoiceRepository.deleteAll();
        paymentRepository.deleteAll();
    }

    private Payment createApprovedPayment() {
        Payment payment = Payment.builder()
                .amount(BigDecimal.valueOf(99.99))
                .currency("USD")
                .method("CREDIT_CARD")
                .status(PaymentStatus.APPROVED)
                .userId("user123")
                .orderId("order456")
                .createdAt(java.time.LocalDateTime.now())
                .updatedAt(java.time.LocalDateTime.now())
                .build();
        return paymentRepository.save(payment);
    }

    private Invoice createInvoice(Payment payment, InvoiceStatus status) {
        // Generate unique invoice number to avoid conflicts
        String uniqueInvoiceNumber = "INV-TEST-" + System.currentTimeMillis() + "-" + payment.getId();
        Invoice invoice = Invoice.builder()
                .invoiceNumber(uniqueInvoiceNumber)
                .paymentId(payment.getId())
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(status)
                .issueDate(LocalDate.now())
                .orderId(payment.getOrderId())
                .build();
        return invoiceRepository.save(invoice);
    }

    @Test
    void updateInvoiceStatus_FromCreatedToSent_ShouldReturn200() throws Exception {
        // Given
        Payment payment = createApprovedPayment();
        Invoice invoice = createInvoice(payment, InvoiceStatus.CREATED);
        
        UpdateInvoiceStatusRequest request = new UpdateInvoiceStatusRequest();
        request.setStatus(UpdateInvoiceStatusRequest.StatusEnum.SENT);

        // When/Then
        mockMvc.perform(patch("/invoices/{id}", invoice.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(invoice.getId()))
                .andExpect(jsonPath("$.status").value("SENT"))
                .andExpect(jsonPath("$.sentAt").exists());

        // Verify in database
        Invoice updated = invoiceRepository.findById(invoice.getId()).orElseThrow();
        assertEquals(InvoiceStatus.SENT, updated.getStatus());
        assertNotNull(updated.getSentAt());
    }

    @Test
    void updateInvoiceStatus_FromCreatedToPaid_ShouldReturn200() throws Exception {
        // Given
        Payment payment = createApprovedPayment();
        Invoice invoice = createInvoice(payment, InvoiceStatus.CREATED);
        
        UpdateInvoiceStatusRequest request = new UpdateInvoiceStatusRequest();
        request.setStatus(UpdateInvoiceStatusRequest.StatusEnum.PAID);

        // When/Then
        mockMvc.perform(patch("/invoices/{id}", invoice.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.paidAt").exists());

        // Verify in database
        Invoice updated = invoiceRepository.findById(invoice.getId()).orElseThrow();
        assertEquals(InvoiceStatus.PAID, updated.getStatus());
        assertNotNull(updated.getPaidAt());
    }

    @Test
    void updateInvoiceStatus_FromCreatedToCancelled_ShouldReturn200() throws Exception {
        // Given
        Payment payment = createApprovedPayment();
        Invoice invoice = createInvoice(payment, InvoiceStatus.CREATED);
        
        UpdateInvoiceStatusRequest request = new UpdateInvoiceStatusRequest();
        request.setStatus(UpdateInvoiceStatusRequest.StatusEnum.CANCELLED);

        // When/Then
        mockMvc.perform(patch("/invoices/{id}", invoice.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.cancelledAt").exists());

        // Verify in database
        Invoice updated = invoiceRepository.findById(invoice.getId()).orElseThrow();
        assertEquals(InvoiceStatus.CANCELLED, updated.getStatus());
        assertNotNull(updated.getCancelledAt());
    }

    @Test
    void updateInvoiceStatus_FromSentToPaid_ShouldReturn200() throws Exception {
        // Given
        Payment payment = createApprovedPayment();
        Invoice invoice = createInvoice(payment, InvoiceStatus.SENT);
        invoice.setSentAt(LocalDate.now().minusDays(1));
        invoiceRepository.save(invoice);
        
        UpdateInvoiceStatusRequest request = new UpdateInvoiceStatusRequest();
        request.setStatus(UpdateInvoiceStatusRequest.StatusEnum.PAID);

        // When/Then
        mockMvc.perform(patch("/invoices/{id}", invoice.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.paidAt").exists());

        // Verify in database
        Invoice updated = invoiceRepository.findById(invoice.getId()).orElseThrow();
        assertEquals(InvoiceStatus.PAID, updated.getStatus());
        assertNotNull(updated.getPaidAt());
    }

    @Test
    void updateInvoiceStatus_WithInvalidBackwardTransition_ShouldReturn409() throws Exception {
        // Given
        Payment payment = createApprovedPayment();
        Invoice invoice = createInvoice(payment, InvoiceStatus.SENT);
        
        UpdateInvoiceStatusRequest request = new UpdateInvoiceStatusRequest();
        request.setStatus(UpdateInvoiceStatusRequest.StatusEnum.SENT); // Trying to go back to CREATED is invalid, but SENT->SENT is same status

        // When/Then - trying SENT -> CREATED (backward transition)
        // Note: We can't test SENT -> CREATED directly since UpdateInvoiceStatusRequest only allows SENT, PAID, CANCELLED
        // But we can test PAID -> SENT which is also invalid
        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaidAt(LocalDate.now());
        invoiceRepository.save(invoice);
        
        request.setStatus(UpdateInvoiceStatusRequest.StatusEnum.SENT);

        mockMvc.perform(patch("/invoices/{id}", invoice.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(409))
                .andExpect(jsonPath("$.message").value("Invalid status transition"));
    }

    @Test
    void updateInvoiceStatus_FromTerminalStatePaid_ShouldReturn409() throws Exception {
        // Given
        Payment payment = createApprovedPayment();
        Invoice invoice = createInvoice(payment, InvoiceStatus.PAID);
        invoice.setPaidAt(LocalDate.now());
        invoiceRepository.save(invoice);
        
        UpdateInvoiceStatusRequest request = new UpdateInvoiceStatusRequest();
        request.setStatus(UpdateInvoiceStatusRequest.StatusEnum.CANCELLED);

        // When/Then
        mockMvc.perform(patch("/invoices/{id}", invoice.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(409))
                .andExpect(jsonPath("$.message").value("Invalid status transition"))
                .andExpect(jsonPath("$.details").value(org.hamcrest.Matchers.containsString("terminal state")));
    }

    @Test
    void updateInvoiceStatus_FromTerminalStateCancelled_ShouldReturn409() throws Exception {
        // Given
        Payment payment = createApprovedPayment();
        Invoice invoice = createInvoice(payment, InvoiceStatus.CANCELLED);
        invoice.setCancelledAt(LocalDate.now());
        invoiceRepository.save(invoice);
        
        UpdateInvoiceStatusRequest request = new UpdateInvoiceStatusRequest();
        request.setStatus(UpdateInvoiceStatusRequest.StatusEnum.PAID);

        // When/Then
        mockMvc.perform(patch("/invoices/{id}", invoice.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(409))
                .andExpect(jsonPath("$.message").value("Invalid status transition"));
    }

    @Test
    void updateInvoiceStatus_WithNonExistentInvoice_ShouldReturn404() throws Exception {
        // Given
        UpdateInvoiceStatusRequest request = new UpdateInvoiceStatusRequest();
        request.setStatus(UpdateInvoiceStatusRequest.StatusEnum.SENT);

        // When/Then
        mockMvc.perform(patch("/invoices/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("Invoice not found"));
    }

    @Test
    void updateInvoiceStatus_WithInvalidStatusValue_ShouldReturn400() throws Exception {
        // Given
        Payment payment = createApprovedPayment();
        Invoice invoice = createInvoice(payment, InvoiceStatus.CREATED);
        
        // Create invalid request with invalid status (using JSON directly)
        String invalidRequest = "{\"status\":\"INVALID_STATUS\"}";

        // When/Then
        mockMvc.perform(patch("/invoices/{id}", invoice.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }

    // Story C2: Invoice Retrieval Tests
    @Test
    void getInvoiceById_WithValidId_ShouldReturn200() throws Exception {
        // Given
        Payment payment = createApprovedPayment();
        Invoice invoice = createInvoice(payment, InvoiceStatus.CREATED);

        // When/Then
        mockMvc.perform(get("/invoices/{id}", invoice.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(invoice.getId()))
                .andExpect(jsonPath("$.invoiceNumber").value(invoice.getInvoiceNumber()))
                .andExpect(jsonPath("$.paymentId").value(payment.getId()))
                .andExpect(jsonPath("$.userId").value(payment.getUserId()))
                .andExpect(jsonPath("$.amount").value(99.99))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.issueDate").exists())
                .andExpect(jsonPath("$.orderId").value("order456"));
    }

    @Test
    void getInvoiceById_WithNonExistentId_ShouldReturn404() throws Exception {
        // When/Then
        mockMvc.perform(get("/invoices/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("Invoice not found"));
    }

    @Test
    void getInvoiceByPaymentId_WithValidPaymentId_ShouldReturn200() throws Exception {
        // Given
        Payment payment = createApprovedPayment();
        Invoice invoice = createInvoice(payment, InvoiceStatus.CREATED);

        // When/Then
        mockMvc.perform(get("/invoices")
                        .param("paymentId", String.valueOf(payment.getId())))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(invoice.getId()))
                .andExpect(jsonPath("$.invoiceNumber").value(invoice.getInvoiceNumber()))
                .andExpect(jsonPath("$.paymentId").value(payment.getId()))
                .andExpect(jsonPath("$.userId").value(payment.getUserId()))
                .andExpect(jsonPath("$.amount").value(99.99))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.issueDate").exists())
                .andExpect(jsonPath("$.orderId").value("order456"));
    }

    @Test
    void getInvoiceByPaymentId_WithNonExistentPaymentId_ShouldReturn404() throws Exception {
        // Given
        Payment payment = createApprovedPayment();
        // No invoice created for this payment

        // When/Then
        mockMvc.perform(get("/invoices")
                        .param("paymentId", String.valueOf(payment.getId())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("Invoice not found"));
    }

    @Test
    void getInvoiceById_WithAllStoryC2Fields_ShouldReturnCompleteInvoice() throws Exception {
        // Given - Create invoice with all fields populated
        Payment payment = createApprovedPayment();
        Invoice invoice = createInvoice(payment, InvoiceStatus.SENT);
        invoice.setSentAt(LocalDate.now().minusDays(1));
        invoice.setDueDate(LocalDate.now().plusDays(30));
        invoiceRepository.save(invoice);

        // When/Then - Verify all Story C2 requirements are met:
        // - montant (amount)
        // - devise (currency)
        // - dates (issueDate, dueDate, sentAt)
        // - statut (status)
        // - références paiement (paymentId)
        mockMvc.perform(get("/invoices/{id}", invoice.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").exists()) // montant
                .andExpect(jsonPath("$.currency").exists()) // devise
                .andExpect(jsonPath("$.issueDate").exists()) // dates
                .andExpect(jsonPath("$.dueDate").exists()) // dates
                .andExpect(jsonPath("$.sentAt").exists()) // dates
                .andExpect(jsonPath("$.status").exists()) // statut
                .andExpect(jsonPath("$.paymentId").exists()); // références paiement
    }

    // Story C3: Invoice Listing Tests
    @Test
    void listInvoices_WithNoFilters_ShouldReturnPaginatedList() throws Exception {
        // Given - Create multiple invoices
        for (int i = 0; i < 3; i++) {
            Payment payment = createApprovedPayment();
            Invoice invoice = createInvoice(payment, InvoiceStatus.CREATED);
            invoice.setIssueDate(LocalDate.now().minusDays(i));
            invoiceRepository.save(invoice);
        }

        // When/Then
        mockMvc.perform(get("/invoices"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(3)))
                .andExpect(jsonPath("$.totalElements").value(org.hamcrest.Matchers.greaterThanOrEqualTo(3)))
                .andExpect(jsonPath("$.totalPages").exists())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20));
    }

    @Test
    void listInvoices_WithStatusFilter_ShouldReturnFilteredInvoices() throws Exception {
        // Given - Create invoices with different statuses
        Payment payment1 = createApprovedPayment();
        Invoice invoice1 = createInvoice(payment1, InvoiceStatus.CREATED);
        
        Payment payment2 = createApprovedPayment();
        Invoice invoice2 = createInvoice(payment2, InvoiceStatus.SENT);
        invoice2.setSentAt(LocalDate.now());
        invoiceRepository.save(invoice2);

        // When/Then - Filter by CREATED status
        mockMvc.perform(get("/invoices").param("status", "CREATED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].status").value("CREATED"))
                .andExpect(jsonPath("$.totalElements").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));
    }

    @Test
    void listInvoices_WithUserIdFilter_ShouldReturnFilteredInvoices() throws Exception {
        // Given - Create invoice for specific user
        Payment payment = createApprovedPayment();
        payment.setUserId("filterUser");
        paymentRepository.save(payment);
        Invoice invoice = createInvoice(payment, InvoiceStatus.CREATED);

        // When/Then
        mockMvc.perform(get("/invoices").param("userId", "filterUser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].userId").value("filterUser"))
                .andExpect(jsonPath("$.totalElements").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));
    }

    @Test
    void listInvoices_WithDateRangeFilter_ShouldReturnFilteredInvoices() throws Exception {
        // Given - Create invoice with specific date
        Payment payment = createApprovedPayment();
        Invoice invoice = createInvoice(payment, InvoiceStatus.CREATED);
        LocalDate invoiceDate = LocalDate.now().minusDays(5);
        invoice.setIssueDate(invoiceDate);
        invoiceRepository.save(invoice);

        LocalDate fromDate = LocalDate.now().minusDays(7);
        LocalDate toDate = LocalDate.now();

        // When/Then
        mockMvc.perform(get("/invoices")
                        .param("fromDate", fromDate.toString())
                        .param("toDate", toDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));
    }

    @Test
    void listInvoices_WithMultipleFilters_ShouldReturnFilteredInvoices() throws Exception {
        // Given - Create invoice with specific status and userId
        Payment payment = createApprovedPayment();
        payment.setUserId("multiFilterUser");
        paymentRepository.save(payment);
        Invoice invoice = createInvoice(payment, InvoiceStatus.CREATED);

        // When/Then
        mockMvc.perform(get("/invoices")
                        .param("status", "CREATED")
                        .param("userId", "multiFilterUser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].status").value("CREATED"))
                .andExpect(jsonPath("$.content[0].userId").value("multiFilterUser"))
                .andExpect(jsonPath("$.totalElements").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));
    }

    @Test
    void listInvoices_WithPagination_ShouldReturnPaginatedResults() throws Exception {
        // Given - Create multiple invoices
        for (int i = 0; i < 5; i++) {
            Payment payment = createApprovedPayment();
            Invoice invoice = createInvoice(payment, InvoiceStatus.CREATED);
            invoice.setIssueDate(LocalDate.now().minusDays(i));
            invoiceRepository.save(invoice);
        }

        // When/Then - Request page 0 with size 2
        mockMvc.perform(get("/invoices")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(org.hamcrest.Matchers.greaterThanOrEqualTo(5)));
    }

    @Test
    void listInvoices_WithInvalidPage_ShouldReturn400() throws Exception {
        // When/Then - Invalid page number
        mockMvc.perform(get("/invoices").param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void listInvoices_WithInvalidSize_ShouldReturn400() throws Exception {
        // When/Then - Invalid size
        mockMvc.perform(get("/invoices").param("size", "200"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void listInvoices_WithInvalidDateRange_ShouldReturn400() throws Exception {
        // When/Then - Invalid date range
        LocalDate fromDate = LocalDate.now();
        LocalDate toDate = LocalDate.now().minusDays(1);
        mockMvc.perform(get("/invoices")
                        .param("fromDate", fromDate.toString())
                        .param("toDate", toDate.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void listInvoices_WithPaymentId_ShouldReturnSingleInvoice() throws Exception {
        // Given - Backward compatibility: when paymentId is provided, return single invoice
        Payment payment = createApprovedPayment();
        Invoice invoice = createInvoice(payment, InvoiceStatus.CREATED);

        // When/Then - Should return single InvoiceResponse, not InvoiceListResponse
        mockMvc.perform(get("/invoices").param("paymentId", String.valueOf(payment.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(invoice.getId()))
                .andExpect(jsonPath("$.invoiceNumber").value(invoice.getInvoiceNumber()))
                .andExpect(jsonPath("$.content").doesNotExist()) // Not a list response
                .andExpect(jsonPath("$.totalElements").doesNotExist()); // Not a list response
    }
}

