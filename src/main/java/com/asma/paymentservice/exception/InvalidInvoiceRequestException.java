package com.asma.paymentservice.exception;

public class InvalidInvoiceRequestException extends RuntimeException {
    public InvalidInvoiceRequestException(String message) {
        super(message);
    }
}

