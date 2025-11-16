package com.asma.paymentservice.exception;

import com.asma.paymentservice.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.stream.Collectors;

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
        String message = ex.getMessage();
        // Validation errors (pagination, date range) should return 400, conflicts return 409
        if (message != null && (message.contains("Page number") || message.contains("Page size") || 
            message.contains("fromDate") || message.contains("Invalid status value"))) {
            error.setCode(400);
            error.setMessage("Validation failed");
        } else {
            error.setCode(409);
            error.setMessage("Invalid invoice request");
        }
        error.setDetails(message);
        return ResponseEntity.status(error.getCode() == 400 ? HttpStatus.BAD_REQUEST : HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        ErrorResponse error = new ErrorResponse();
        error.setCode(400);
        error.setMessage("Invalid request body");
        error.setDetails(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        ErrorResponse error = new ErrorResponse();
        error.setCode(400);
        error.setMessage("Validation failed");
        StringBuilder details = new StringBuilder();
        ex.getBindingResult().getFieldErrors().forEach(fieldError -> {
            if (details.length() > 0) details.append("; ");
            details.append(fieldError.getField()).append(": ").append(fieldError.getDefaultMessage());
        });
        error.setDetails(details.toString());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        ErrorResponse error = new ErrorResponse();
        error.setCode(400);
        error.setMessage("Validation failed");
        String details = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        error.setDetails(details);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
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

