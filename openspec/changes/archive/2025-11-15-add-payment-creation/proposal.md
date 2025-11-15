## Why

Clients (frontend applications or other services) need to create payments to initiate transactions for orders or users. This is the foundational capability for the Payment Service, enabling the payment lifecycle to begin.

## What Changes

- **ADDED**: Payment creation API endpoint (`POST /payments`)
- **ADDED**: Payment entity model with required fields (amount, currency, method, userId, orderId, status, timestamps)
- **ADDED**: Input validation for payment creation requests
- **ADDED**: Payment repository for database persistence
- **ADDED**: Payment service layer with business logic
- **ADDED**: OpenAPI specification for payment creation contract

## Impact

- **Affected specs**: New capability `payment` (payment creation)
- **Affected code**: 
  - New OpenAPI spec file (to be defined)
  - New Payment entity (`com.asma.paymentservice.entity.Payment`)
  - New PaymentRepository (`com.asma.paymentservice.repository.PaymentRepository`)
  - New PaymentService (`com.asma.paymentservice.service.PaymentService`)
  - Generated PaymentController (from OpenAPI spec)
  - Generated DTOs (from OpenAPI spec)
  - Exception handling for validation errors
- **Database**: New `payment` table schema

