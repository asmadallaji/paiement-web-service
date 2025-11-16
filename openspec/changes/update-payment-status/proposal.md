## Why

The system and back-office administrators need to update payment statuses to reflect the actual transaction results. This enables the payment lifecycle management, allowing payments to transition from PENDING to APPROVED, FAILED, or CANCELED states based on external payment gateway responses or administrative actions.

## What Changes

- **ADDED**: PATCH `/payments/{id}/status` endpoint to update payment status
- **ADDED**: Status transition validation enforcing business rules:
  - PENDING → APPROVED
  - PENDING → FAILED
  - PENDING → CANCELED
  - No changes allowed after APPROVED, FAILED, or CANCELED
- **ADDED**: Service method to update payment status with transition validation
- **ADDED**: Exception handling for invalid status transitions (409 Conflict)
- **ADDED**: Automatic `updatedAt` timestamp update on status change
- **ADDED**: OpenAPI specification for the PATCH endpoint with request/response schemas
- **ADDED**: Unit and integration tests for status update scenarios

## Impact

- **Affected specs**: Payment capability (adds status update requirement)
- **Affected code**: 
  - `PaymentController` - Add PATCH endpoint implementation
  - `PaymentService` - Add `updatePaymentStatus` method with transition validation
  - `PaymentRepository` - Use existing `save` method from JpaRepository
  - `openapi/payment-api.yaml` - Add PATCH `/payments/{id}/status` endpoint definition
  - Exception handling - Add `InvalidStatusTransitionException` for 409 responses
- **Behavior change**: New status update capability, no breaking changes to existing endpoints

