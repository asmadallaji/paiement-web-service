package com.asma.paymentservice.controller;

import com.asma.paymentservice.api.PaymentsApi;
import com.asma.paymentservice.dto.CreatePaymentRequest;
import com.asma.paymentservice.dto.PaymentResponse;
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
}

