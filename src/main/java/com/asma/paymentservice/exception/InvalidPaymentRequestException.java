package com.asma.paymentservice.exception;

public class InvalidPaymentRequestException extends RuntimeException {
    public InvalidPaymentRequestException(String message) {
        super(message);
    }
}

