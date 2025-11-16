## 1. Domain Model
- [x] 1.1 Add optional date fields to Invoice entity: `sentAt` (LocalDate, nullable), `paidAt` (LocalDate, nullable), `cancelledAt` (LocalDate, nullable)
- [x] 1.2 Update Invoice entity JPA annotations to include new date columns
- [x] 1.3 Ensure Lombok annotations support the new fields

## 2. Service Layer
- [x] 2.1 Add `updateInvoiceStatus(Long id, InvoiceStatus newStatus)` method to InvoiceService
- [x] 2.2 Implement `validateStatusTransition(Long invoiceId, InvoiceStatus currentStatus, InvoiceStatus targetStatus)` method:
  - [x] 2.2.1 Validate target status is not null
  - [x] 2.2.2 Reject same-status transitions
  - [x] 2.2.3 Reject transitions from terminal states (PAID, CANCELLED)
  - [x] 2.2.4 Validate allowed transitions: CREATED → SENT, CREATED → PAID, CREATED → CANCELLED, SENT → PAID
  - [x] 2.2.5 Reject backward transitions (e.g., SENT → CREATED, PAID → SENT)
  - [x] 2.2.6 Reject invalid status values
  - [x] 2.2.7 Log business rule violations
- [x] 2.3 In updateInvoiceStatus, set appropriate date fields when status changes:
  - [x] 2.3.1 Set sentAt when status changes to SENT
  - [x] 2.3.2 Set paidAt when status changes to PAID
  - [x] 2.3.3 Set cancelledAt when status changes to CANCELLED
- [x] 2.4 Add proper exception handling and logging

## 3. Exception Handling
- [x] 3.1 Create `InvalidInvoiceStatusTransitionException` exception class (or reuse existing InvalidStatusTransitionException if generic)
- [x] 3.2 Update GlobalExceptionHandler to handle invoice status transition exceptions with 409 Conflict

## 4. OpenAPI Specification
- [x] 4.1 Add PATCH /invoices/{id}/status endpoint to OpenAPI spec
- [x] 4.2 Define UpdateInvoiceStatusRequest schema with `status` field (enum: SENT, PAID, CANCELLED)
- [x] 4.3 Define error responses (400 for invalid status value, 404 for not found, 409 for invalid transition, 500 for server errors)
- [x] 4.4 Update InvoiceResponse schema to include optional date fields: sentAt, paidAt, cancelledAt

## 5. Code Generation
- [x] 5.1 Run OpenAPI Generator to generate UpdateInvoiceStatusRequest DTO and updated InvoicesApi interface
- [x] 5.2 Verify generated code is in correct package structure

## 6. Controller Implementation
- [x] 6.1 Update InvoiceController to implement updateInvoiceStatus method from generated InvoicesApi interface
- [x] 6.2 Convert DTO StatusEnum to entity InvoiceStatus enum
- [x] 6.3 Call InvoiceService.updateInvoiceStatus
- [x] 6.4 Map updated invoice to InvoiceResponse DTO
- [x] 6.5 Add proper exception handling and error responses

## 7. Testing
- [x] 7.1 Write unit tests for InvoiceService.validateStatusTransition:
  - [x] 7.1.1 Test valid transitions (CREATED → SENT, CREATED → PAID, CREATED → CANCELLED, SENT → PAID)
  - [x] 7.1.2 Test invalid backward transitions (SENT → CREATED, PAID → SENT)
  - [x] 7.1.3 Test invalid transitions from terminal states (PAID → any, CANCELLED → any)
  - [x] 7.1.4 Test same-status transition rejection
  - [x] 7.1.5 Test null target status rejection
  - [x] 7.1.6 Test invalid status value rejection
- [x] 7.2 Write unit tests for InvoiceService.updateInvoiceStatus:
  - [x] 7.2.1 Test successful status update from CREATED to SENT (verify sentAt is set)
  - [x] 7.2.2 Test successful status update from CREATED to PAID (verify paidAt is set)
  - [x] 7.2.3 Test successful status update from CREATED to CANCELLED (verify cancelledAt is set)
  - [x] 7.2.4 Test successful status update from SENT to PAID (verify paidAt is set)
  - [x] 7.2.5 Test rejection of invalid transitions
  - [x] 7.2.6 Test 404 for non-existent invoice
- [x] 7.3 Write integration tests for InvoiceController:
  - [x] 7.3.1 Test PATCH /invoices/{id}/status endpoint with valid transitions
  - [x] 7.3.2 Test PATCH /invoices/{id}/status with invalid backward transitions (409)
  - [x] 7.3.3 Test PATCH /invoices/{id}/status with terminal state (409)
  - [x] 7.3.4 Test PATCH /invoices/{id}/status with non-existent invoice (404)
  - [x] 7.3.5 Test PATCH /invoices/{id}/status with invalid status value (400)

## 8. Database Migration
- [x] 8.1 Update database schema to add sentAt, paidAt, cancelledAt columns to invoice table (nullable LocalDate columns)
  - Note: Schema automatically updated via Hibernate `ddl-auto=update` when Invoice entity fields are added
- [x] 8.2 Verify existing invoices remain valid with null date fields
  - Verified: Integration tests confirm existing invoices work correctly with null date fields

## 9. Validation
- [x] 9.1 Run `openspec validate update-invoice-status --strict`
- [x] 9.2 Fix any validation errors
- [x] 9.3 Ensure all scenarios are covered by tests

