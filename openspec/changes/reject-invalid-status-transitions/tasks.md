## 1. Implementation

- [x] 1.1 Create `InvalidStatusTransitionException` exception class in `exception` package
- [x] 1.2 Create centralized `validateStatusTransition` method in `PaymentService` that:
  - Accepts current status and target status
  - Validates that target status is a valid PaymentStatus enum value
  - Validates allowed transitions (PENDING → APPROVED/FAILED/CANCELED only)
  - Rejects transitions from terminal states (APPROVED, FAILED, CANCELED)
  - Throws `InvalidStatusTransitionException` with descriptive message on validation failure
- [x] 1.3 Integrate transition validation into status update logic (when status update endpoint is implemented)
- [x] 1.4 Update `GlobalExceptionHandler` to catch `InvalidStatusTransitionException` and return 409 Conflict with ErrorResponse
- [x] 1.5 Add logging in validation method to log business rule violations with payment ID, current status, and target status

## 2. Testing

- [x] 2.1 Unit test: Valid transitions (PENDING → APPROVED, PENDING → FAILED, PENDING → CANCELED)
- [x] 2.2 Unit test: Invalid transition from terminal state (APPROVED → PENDING)
- [x] 2.3 Unit test: Invalid transition from terminal state (FAILED → APPROVED)
- [x] 2.4 Unit test: Invalid transition from terminal state (CANCELED → PENDING)
- [x] 2.5 Unit test: Invalid target status (null value)
- [x] 2.6 Unit test: Same status transition (PENDING → PENDING is rejected)
- [x] 2.7 Integration test: Status update endpoint returns 409 for invalid transitions
- [x] 2.8 Integration test: Error response format includes code 409, message, and details

## 3. Documentation

- [x] 3.1 Update OpenAPI spec to document 409 Conflict response for invalid status transitions
- [x] 3.2 Add error response examples in OpenAPI spec showing invalid transition scenarios

