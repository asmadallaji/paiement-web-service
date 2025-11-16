package com.asma.paymentservice.exception;

import com.asma.paymentservice.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidPaymentRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPaymentRequest(InvalidPaymentRequestException ex) {
        ErrorResponse error = new ErrorResponse();
        error.setCode(400);
        error.setMessage("Validation failed");
        error.setDetails(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePaymentNotFound(PaymentNotFoundException ex) {
        ErrorResponse error = new ErrorResponse();
        error.setCode(404);
        error.setMessage("Payment not found");
        error.setDetails(ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(InvalidStatusTransitionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidStatusTransition(InvalidStatusTransitionException ex) {
        ErrorResponse error = new ErrorResponse();
        error.setCode(409);
        error.setMessage("Invalid status transition");
        error.setDetails(ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(InvoiceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleInvoiceNotFound(InvoiceNotFoundException ex) {
        ErrorResponse error = new ErrorResponse();
        error.setCode(404);
        error.setMessage("Invoice not found");
        error.setDetails(ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(InvalidInvoiceRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalidInvoiceRequest(InvalidInvoiceRequestException ex) {
        ErrorResponse error = new ErrorResponse();
        error.setCode(409);
        error.setMessage("Invalid invoice request");
        error.setDetails(ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse error = new ErrorResponse();
        error.setCode(500);
        error.setMessage("Internal server error");
        error.setDetails(ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}

