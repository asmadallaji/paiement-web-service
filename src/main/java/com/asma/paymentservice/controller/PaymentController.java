package com.asma.paymentservice.controller;

import com.asma.paymentservice.api.PaymentsApi;
import com.asma.paymentservice.dto.CreatePaymentRequest;
import com.asma.paymentservice.dto.PaymentListResponse;
import com.asma.paymentservice.dto.PaymentResponse;
import com.asma.paymentservice.dto.UpdatePaymentStatusRequest;
import com.asma.paymentservice.entity.PaymentStatus;
import com.asma.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PaymentController implements PaymentsApi {

    private final PaymentService paymentService;

    @Override
    public ResponseEntity<PaymentResponse> createPayment(CreatePaymentRequest createPaymentRequest) {
        PaymentResponse response = paymentService.createPayment(createPaymentRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<PaymentResponse> getPaymentById(Long id) {
        PaymentResponse response = paymentService.getPaymentById(id);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<PaymentListResponse> listPayments(String status, String userId, String orderId, Integer page, Integer size) {
        PaymentListResponse response = paymentService.listPayments(status, userId, orderId, page, size);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<PaymentResponse> updatePaymentStatus(Long id, UpdatePaymentStatusRequest updatePaymentStatusRequest) {
        // Convert DTO StatusEnum to entity PaymentStatus
        PaymentStatus newStatus = PaymentStatus.valueOf(updatePaymentStatusRequest.getStatus().getValue());
        PaymentResponse response = paymentService.updatePaymentStatus(id, newStatus);
        return ResponseEntity.ok(response);
    }
}

