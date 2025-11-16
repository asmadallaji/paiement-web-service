## 1. OpenAPI Specification
- [x] 1.1 Add GET `/payments` endpoint to `payment-api.yaml`
- [x] 1.2 Define query parameters: `status` (optional enum), `userId` (optional string), `orderId` (optional string)
- [x] 1.3 Define pagination query parameters: `page` (optional integer, default 0), `size` (optional integer, default 20, max 100)
- [x] 1.4 Create `PaymentListResponse` schema with pagination metadata (content, totalElements, totalPages, page, size)
- [x] 1.5 Define response schemas: 200 (PaymentListResponse), 400 (ErrorResponse for invalid parameters)
- [x] 1.6 Regenerate DTOs and API interfaces using OpenAPI Generator

## 2. Repository Layer
- [x] 2.1 Add query methods to `PaymentRepository` for filtering:
  - `findByStatus` (when status filter provided)
  - `findByUserId` (when userId filter provided)
  - `findByOrderId` (when orderId filter provided)
  - `findByStatusAndUserId` (when both filters provided)
  - `findByStatusAndOrderId` (when status and orderId filters provided)
  - `findByUserIdAndOrderId` (when userId and orderId filters provided)
  - `findByStatusAndUserIdAndOrderId` (when all filters provided)
- [x] 2.2 Use Spring Data JPA's `Pageable` interface for pagination support
- [x] 2.3 Ensure all query methods return `Page<Payment>` for consistent pagination

## 3. Service Layer
- [x] 3.1 Add `listPayments` method to `PaymentService` with filter parameters and pagination
- [x] 3.2 Implement filter logic to select appropriate repository method based on provided filters
- [x] 3.3 Validate pagination parameters (page >= 0, size between 1 and 100)
- [x] 3.4 Map `Page<Payment>` to `PaymentListResponse` DTO
- [x] 3.5 Add logging for payment listing operations
- [x] 3.6 Handle default pagination (page 0, size 20)

## 4. Controller Layer
- [x] 4.1 Implement `listPayments` method in `PaymentController`
- [x] 4.2 Map query parameters to service method parameters
- [x] 4.3 Return 200 OK with PaymentListResponse when successful
- [x] 4.4 Handle invalid pagination parameters (return 400 Bad Request)

## 5. Exception Handling
- [x] 5.1 Create `InvalidPaginationException` for invalid pagination parameters (optional, can reuse existing exceptions)
- [x] 5.2 Add validation for pagination parameters in service layer
- [x] 5.3 Map validation errors to ErrorResponse DTO

## 6. Testing
- [x] 6.1 Unit test: `listPayments` returns all payments when no filters
- [x] 6.2 Unit test: `listPayments` filters by status correctly
- [x] 6.3 Unit test: `listPayments` filters by userId correctly
- [x] 6.4 Unit test: `listPayments` filters by orderId correctly
- [x] 6.5 Unit test: `listPayments` applies multiple filters correctly
- [x] 6.6 Unit test: `listPayments` handles pagination correctly
- [x] 6.7 Unit test: `listPayments` validates pagination parameters
- [x] 6.8 Integration test: GET `/payments` returns paginated list
- [x] 6.9 Integration test: GET `/payments?status=PENDING` filters correctly
- [x] 6.10 Integration test: GET `/payments?userId=user123` filters correctly
- [x] 6.11 Integration test: GET `/payments?orderId=order456` filters correctly
- [x] 6.12 Integration test: GET `/payments?status=PENDING&userId=user123` applies multiple filters
- [x] 6.13 Integration test: GET `/payments?page=0&size=10` paginates correctly
- [x] 6.14 Integration test: GET `/payments?page=1&size=5` handles page navigation
- [x] 6.15 Integration test: GET `/payments?size=200` returns 400 for invalid size

## 7. Validation
- [x] 7.1 Run `openspec validate list-payments-with-filters --strict` and resolve any issues
- [x] 7.2 Verify payment listing works correctly in integration tests
- [x] 7.3 Verify OpenAPI spec is valid and generates correct code

