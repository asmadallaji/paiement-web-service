## Why

Users, administrators, and other services need to retrieve payment details by ID to view payment status and information. This is a fundamental read operation required for payment management and integration with other services.

## What Changes

- **ADDED**: GET `/payments/{id}` endpoint to retrieve a payment by its unique identifier
- **ADDED**: Payment retrieval service method to fetch payment from repository
- **ADDED**: Exception handling for payment not found (404 Not Found)
- **ADDED**: OpenAPI specification for the GET endpoint with proper response schemas
- **ADDED**: Unit and integration tests for payment retrieval scenarios

## Impact

- **Affected specs**: Payment capability (adds retrieval requirement)
- **Affected code**: 
  - `PaymentController` - Add GET endpoint implementation
  - `PaymentService` - Add `getPaymentById` method
  - `PaymentRepository` - Use existing `findById` method from JpaRepository
  - `openapi/payment-api.yaml` - Add GET `/payments/{id}` endpoint definition
- **Behavior change**: New read-only endpoint, no breaking changes

