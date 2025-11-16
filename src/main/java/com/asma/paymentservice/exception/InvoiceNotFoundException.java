package com.asma.paymentservice.exception;

public class InvoiceNotFoundException extends RuntimeException {
    public InvoiceNotFoundException(String message) {
        super(message);
    }

    public InvoiceNotFoundException(Long id) {
        super("Invoice not found with ID: " + id);
    }
}

