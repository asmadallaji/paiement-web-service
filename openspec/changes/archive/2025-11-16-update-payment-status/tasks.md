## 1. OpenAPI Specification
- [x] 1.1 Add PATCH `/payments/{id}/status` endpoint to `payment-api.yaml`
- [x] 1.2 Define path parameter `id` (integer, required)
- [x] 1.3 Define request body schema with `status` field (enum: APPROVED, FAILED, CANCELED)
- [x] 1.4 Define response schemas: 200 (PaymentResponse), 404 (ErrorResponse), 409 (ErrorResponse)
- [x] 1.5 Regenerate DTOs and API interfaces using OpenAPI Generator (requires mvn generate-sources)

## 2. Service Layer
- [x] 2.1 Add `updatePaymentStatus(Long id, PaymentStatus newStatus)` method to `PaymentService`
- [x] 2.2 Implement status transition validation logic:
  - Allow PENDING → APPROVED, FAILED, CANCELED
  - Reject transitions from APPROVED, FAILED, CANCELED
  - Reject invalid status values
- [x] 2.3 Fetch payment from repository and validate existence
- [x] 2.4 Update payment status and `updatedAt` timestamp
- [x] 2.5 Save updated payment to repository
- [x] 2.6 Map Payment entity to PaymentResponse DTO
- [x] 2.7 Add logging for status updates and invalid transitions

## 3. Exception Handling
- [x] 3.1 Create `InvalidStatusTransitionException` custom exception
- [x] 3.2 Add exception handler in `GlobalExceptionHandler` for 409 Conflict responses
- [x] 3.3 Map exception to ErrorResponse DTO with clear error message

## 4. Controller Layer
- [x] 4.1 Implement `updatePaymentStatus` method in `PaymentController`
- [x] 4.2 Handle `PaymentNotFoundException` and return 404 status
- [x] 4.3 Handle `InvalidStatusTransitionException` and return 409 status
- [x] 4.4 Return 200 OK with PaymentResponse when status update succeeds

## 5. Testing
- [x] 5.1 Unit test: `updatePaymentStatus` successfully updates PENDING → APPROVED
- [x] 5.2 Unit test: `updatePaymentStatus` successfully updates PENDING → FAILED
- [x] 5.3 Unit test: `updatePaymentStatus` successfully updates PENDING → CANCELED
- [x] 5.4 Unit test: `updatePaymentStatus` throws exception when payment not found
- [x] 5.5 Unit test: `updatePaymentStatus` throws exception for APPROVED → PENDING transition
- [x] 5.6 Unit test: `updatePaymentStatus` throws exception for FAILED → APPROVED transition
- [x] 5.7 Unit test: `updatePaymentStatus` throws exception for CANCELED → APPROVED transition
- [x] 5.8 Unit test: `updatePaymentStatus` updates `updatedAt` timestamp
- [x] 5.9 Integration test: PATCH `/payments/{id}/status` returns 200 with updated payment
- [x] 5.10 Integration test: PATCH `/payments/{id}/status` returns 404 when payment not found
- [x] 5.11 Integration test: PATCH `/payments/{id}/status` returns 409 for invalid transition
- [x] 5.12 Integration test: PATCH `/payments/{id}/status` validates all allowed transitions

## 6. Validation
- [x] 6.1 Run `openspec validate update-payment-status --strict` and resolve any issues
- [x] 6.2 Verify status update works correctly in integration tests
- [x] 6.3 Verify OpenAPI spec is valid and generates correct code (requires mvn generate-sources)

