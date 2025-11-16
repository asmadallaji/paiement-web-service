## Why

Administrators need to list and filter payments to supervise payment activity. This capability enables monitoring, auditing, and administrative oversight of the payment system. Filtering by status, userId, and orderId allows administrators to quickly find specific payments, while pagination ensures efficient handling of large datasets.

## What Changes

- **ADDED**: GET `/payments` endpoint to list payments with optional filters
- **ADDED**: Query parameters for filtering: `status`, `userId`, `orderId`
- **ADDED**: Pagination support with `page` and `size` query parameters
- **ADDED**: Response schema for paginated payment list (PaymentListResponse)
- **ADDED**: Repository methods to support filtering and pagination queries
- **ADDED**: Service layer method to handle payment listing with filters and pagination
- **ADDED**: Unit and integration tests for listing and filtering scenarios

## Impact

- **Affected specs**: Payment capability (adds listing and filtering requirement)
- **Affected code**: 
  - `PaymentController` - Add GET `/payments` endpoint implementation
  - `PaymentService` - Add `listPayments` method with filter and pagination logic
  - `PaymentRepository` - Add query methods for filtering by status, userId, orderId
  - `openapi/payment-api.yaml` - Add GET `/payments` endpoint with query parameters and response schema
- **Behavior change**: New read-only endpoint for listing payments, no breaking changes

