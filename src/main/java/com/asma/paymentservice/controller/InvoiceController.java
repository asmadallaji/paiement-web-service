package com.asma.paymentservice.controller;

import com.asma.paymentservice.api.InvoicesApi;
import com.asma.paymentservice.dto.CreateInvoiceRequest;
import com.asma.paymentservice.dto.InvoiceResponse;
import com.asma.paymentservice.dto.ListInvoices200Response;
import com.asma.paymentservice.dto.UpdateInvoiceStatusRequest;
import com.asma.paymentservice.entity.Invoice;
import com.asma.paymentservice.entity.InvoiceStatus;
import com.asma.paymentservice.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
public class InvoiceController implements InvoicesApi {

    private final InvoiceService invoiceService;

    @Override
    public ResponseEntity<InvoiceResponse> createInvoice(CreateInvoiceRequest createInvoiceRequest) {
        Invoice invoice = invoiceService.createInvoiceManually(createInvoiceRequest.getPaymentId());
        InvoiceResponse response = mapToResponse(invoice);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<ListInvoices200Response> listInvoices(Long paymentId, String status, String userId, LocalDate fromDate, LocalDate toDate, Integer page, Integer size) {
        // If paymentId is provided, return single invoice (backward compatibility)
        if (paymentId != null) {
            Invoice invoice = invoiceService.getInvoiceByPaymentId(paymentId);
            InvoiceResponse response = mapToResponse(invoice);
            return ResponseEntity.ok(response);
        }
        
        // Otherwise, return paginated list with filters
        com.asma.paymentservice.dto.InvoiceListResponse listResponse = invoiceService.listInvoices(status, userId, fromDate, toDate, page, size);
        return ResponseEntity.ok(listResponse);
    }

    @Override
    public ResponseEntity<InvoiceResponse> getInvoiceById(Long id) {
        Invoice invoice = invoiceService.getInvoiceById(id);
        InvoiceResponse response = mapToResponse(invoice);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<InvoiceResponse> updateInvoiceStatus(Long id, UpdateInvoiceStatusRequest updateInvoiceStatusRequest) {
        // Convert DTO StatusEnum to entity InvoiceStatus
        InvoiceStatus newStatus = InvoiceStatus.valueOf(updateInvoiceStatusRequest.getStatus().getValue());
        Invoice invoice = invoiceService.updateInvoiceStatus(id, newStatus);
        InvoiceResponse response = mapToResponse(invoice);
        return ResponseEntity.ok(response);
    }

    private InvoiceResponse mapToResponse(Invoice invoice) {
        InvoiceResponse response = new InvoiceResponse();
        response.setId(invoice.getId());
        response.setInvoiceNumber(invoice.getInvoiceNumber());
        response.setPaymentId(invoice.getPaymentId());
        response.setUserId(invoice.getUserId());
        response.setAmount(invoice.getAmount().doubleValue());
        response.setCurrency(invoice.getCurrency());
        response.setStatus(InvoiceResponse.StatusEnum.fromValue(invoice.getStatus().name()));
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
}

