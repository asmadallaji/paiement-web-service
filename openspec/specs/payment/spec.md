# payment Specification

## Purpose
TBD - created by archiving change add-payment-creation. Update Purpose after archive.
## Requirements
### Requirement: Payment Creation
The system SHALL provide an API endpoint to create a new payment with required and optional fields, validate input data according to business rules, initialize the payment with appropriate default values, persist it to the database, and return the created payment with its unique identifier. When a payment creation request includes an `orderId`, the system SHALL check for existing PENDING payments with the same `orderId` and `userId` combination and return the existing payment if found, preventing duplicate payment creation.

#### Scenario: Duplicate payment creation with same orderId and userId
- **WHEN** a client sends a payment creation request with an `orderId` and `userId`
- **AND** a PENDING payment already exists with the same `orderId` and `userId`
- **THEN** the system SHALL return the existing payment instead of creating a new one
- **AND** the system SHALL log the duplicate detection
- **AND** the response SHALL have status 201 Created (same as new payment creation)

#### Scenario: Payment creation without orderId
- **WHEN** a client sends a payment creation request without an `orderId` (or with null `orderId`)
- **THEN** the system SHALL create a new payment regardless of existing payments
- **AND** no idempotency check SHALL be performed

#### Scenario: Payment creation with different orderId
- **WHEN** a client sends a payment creation request with a different `orderId` than existing payments
- **THEN** the system SHALL create a new payment
- **AND** the idempotency check SHALL not prevent creation

#### Scenario: Payment creation with same orderId but different userId
- **WHEN** a client sends a payment creation request with an `orderId` that exists for a different `userId`
- **THEN** the system SHALL create a new payment
- **AND** the idempotency check SHALL not prevent creation (idempotency key is orderId + userId)

#### Scenario: Payment creation with same orderId and userId but different status
- **WHEN** a client sends a payment creation request with an `orderId` and `userId` that matches an existing payment
- **AND** the existing payment has a status other than PENDING (e.g., APPROVED, FAILED, CANCELED)
- **THEN** the system SHALL create a new payment
- **AND** the idempotency check SHALL only consider PENDING payments

### Requirement: Payment Retrieval by ID
The system SHALL provide an API endpoint to retrieve a payment by its unique identifier. The endpoint SHALL return all payment details including amount, currency, method, status, userId, orderId, and timestamps. If the payment does not exist, the system SHALL return a 404 Not Found response.

#### Scenario: Retrieve existing payment by ID
- **WHEN** a client sends a GET request to `/payments/{id}` with a valid payment ID
- **AND** the payment exists in the database
- **THEN** the system SHALL return the payment details with status 200 OK
- **AND** the response SHALL include all payment fields: id, amount, currency, method, status, userId, orderId, createdAt, updatedAt

#### Scenario: Retrieve non-existent payment by ID
- **WHEN** a client sends a GET request to `/payments/{id}` with a payment ID that does not exist
- **THEN** the system SHALL return status 404 Not Found
- **AND** the response SHALL include an ErrorResponse with code 404 and an appropriate error message

#### Scenario: Retrieve payment with invalid ID format
- **WHEN** a client sends a GET request to `/payments/{id}` with an invalid ID format (e.g., non-numeric)
- **THEN** the system SHALL return status 400 Bad Request
- **AND** the response SHALL include an ErrorResponse with code 400 and validation error details

### Requirement: Payment Listing with Filters and Pagination
The system SHALL provide an API endpoint to list payments with optional filtering by status, userId, and orderId, and support pagination. The endpoint SHALL return a paginated list of payments with metadata including total count, total pages, current page, and page size. All filter parameters are optional and can be combined. Pagination parameters have default values and maximum limits.

#### Scenario: List all payments without filters
- **WHEN** a client sends a GET request to `/payments` without any query parameters
- **THEN** the system SHALL return a paginated list of all payments with status 200 OK
- **AND** the response SHALL include pagination metadata (totalElements, totalPages, page, size)
- **AND** the default page size SHALL be 20
- **AND** the default page SHALL be 0 (first page)
- **AND** payments SHALL be sorted by createdAt in descending order (most recent first)

#### Scenario: List payments filtered by status
- **WHEN** a client sends a GET request to `/payments?status=PENDING`
- **THEN** the system SHALL return only payments with status PENDING
- **AND** the response SHALL include pagination metadata
- **AND** the totalElements SHALL reflect the count of filtered payments

#### Scenario: List payments filtered by userId
- **WHEN** a client sends a GET request to `/payments?userId=user123`
- **THEN** the system SHALL return only payments for the specified userId
- **AND** the response SHALL include pagination metadata
- **AND** the totalElements SHALL reflect the count of filtered payments

#### Scenario: List payments filtered by orderId
- **WHEN** a client sends a GET request to `/payments?orderId=order456`
- **THEN** the system SHALL return only payments with the specified orderId
- **AND** the response SHALL include pagination metadata
- **AND** the totalElements SHALL reflect the count of filtered payments

#### Scenario: List payments with multiple filters
- **WHEN** a client sends a GET request to `/payments?status=PENDING&userId=user123`
- **THEN** the system SHALL return only payments matching all specified filters
- **AND** the response SHALL include pagination metadata
- **AND** the totalElements SHALL reflect the count of payments matching all filters

#### Scenario: List payments with pagination
- **WHEN** a client sends a GET request to `/payments?page=0&size=10`
- **THEN** the system SHALL return the first 10 payments (page 0)
- **AND** the response SHALL include pagination metadata with page=0, size=10
- **AND** the totalPages SHALL be calculated based on totalElements and size

#### Scenario: Navigate to next page
- **WHEN** a client sends a GET request to `/payments?page=1&size=10`
- **THEN** the system SHALL return payments 11-20 (second page)
- **AND** the response SHALL include pagination metadata with page=1, size=10

#### Scenario: Invalid pagination parameters
- **WHEN** a client sends a GET request to `/payments?page=-1`
- **OR** a client sends a GET request to `/payments?size=0`
- **OR** a client sends a GET request to `/payments?size=200`
- **THEN** the system SHALL return status 400 Bad Request
- **AND** the response SHALL include an ErrorResponse with code 400 and validation error details
- **AND** page SHALL be >= 0
- **AND** size SHALL be between 1 and 100 (inclusive)

#### Scenario: Empty result set
- **WHEN** a client sends a GET request to `/payments?status=APPROVED`
- **AND** no payments exist with status APPROVED
- **THEN** the system SHALL return status 200 OK
- **AND** the response SHALL include an empty content array
- **AND** totalElements SHALL be 0
- **AND** totalPages SHALL be 0

### Requirement: Status Transition Validation
The system SHALL validate all payment status transitions according to business rules and reject invalid transitions with clear error responses. The validation SHALL be centralized in the business layer and SHALL enforce that only valid transitions are allowed: PENDING → APPROVED, PENDING → FAILED, and PENDING → CANCELED. No status changes SHALL be allowed from terminal states (APPROVED, FAILED, CANCELED) to any other state. The system SHALL also reject transitions to unknown or invalid status values.

#### Scenario: Valid transition from PENDING to APPROVED
- **WHEN** a client attempts to update a payment status from PENDING to APPROVED
- **THEN** the system SHALL allow the transition
- **AND** the status update SHALL proceed normally

#### Scenario: Valid transition from PENDING to FAILED
- **WHEN** a client attempts to update a payment status from PENDING to FAILED
- **THEN** the system SHALL allow the transition
- **AND** the status update SHALL proceed normally

#### Scenario: Valid transition from PENDING to CANCELED
- **WHEN** a client attempts to update a payment status from PENDING to CANCELED
- **THEN** the system SHALL allow the transition
- **AND** the status update SHALL proceed normally

#### Scenario: Invalid transition from APPROVED to PENDING
- **WHEN** a client attempts to update a payment status from APPROVED to PENDING
- **THEN** the system SHALL reject the transition
- **AND** the system SHALL return status 409 Conflict
- **AND** the response SHALL include an ErrorResponse with code 409 and a message indicating the invalid transition
- **AND** the system SHALL log the business rule violation with payment ID, current status, and target status

#### Scenario: Invalid transition from FAILED to APPROVED
- **WHEN** a client attempts to update a payment status from FAILED to APPROVED
- **THEN** the system SHALL reject the transition
- **AND** the system SHALL return status 409 Conflict
- **AND** the response SHALL include an ErrorResponse with code 409 and a message indicating the invalid transition
- **AND** the system SHALL log the business rule violation

#### Scenario: Invalid transition from CANCELED to PENDING
- **WHEN** a client attempts to update a payment status from CANCELED to PENDING
- **THEN** the system SHALL reject the transition
- **AND** the system SHALL return status 409 Conflict
- **AND** the response SHALL include an ErrorResponse with code 409 and a message indicating the invalid transition
- **AND** the system SHALL log the business rule violation

#### Scenario: Invalid transition from APPROVED to FAILED
- **WHEN** a client attempts to update a payment status from APPROVED to FAILED
- **THEN** the system SHALL reject the transition
- **AND** the system SHALL return status 409 Conflict
- **AND** the response SHALL include an ErrorResponse with code 409 and a message indicating the invalid transition
- **AND** the system SHALL log the business rule violation

#### Scenario: Invalid target status value
- **WHEN** a client attempts to update a payment status to an unknown or invalid status value
- **THEN** the system SHALL reject the transition
- **AND** the system SHALL return status 409 Conflict
- **AND** the response SHALL include an ErrorResponse with code 409 and a message indicating the invalid status value
- **AND** the system SHALL log the business rule violation

### Requirement: Payment Status Update
The system SHALL provide an API endpoint to update the status of an existing payment. The endpoint SHALL validate status transitions according to business rules, update the payment's `updatedAt` timestamp, and return the updated payment. Only payments in PENDING status SHALL be allowed to transition to APPROVED, FAILED, or CANCELED. Payments in APPROVED, FAILED, or CANCELED status SHALL NOT be allowed to change status.

#### Scenario: Update payment status from PENDING to APPROVED
- **WHEN** a client sends a PATCH request to `/payments/{id}/status` with status `APPROVED`
- **AND** the payment exists and has status `PENDING`
- **THEN** the system SHALL update the payment status to `APPROVED`
- **AND** the system SHALL update the `updatedAt` timestamp
- **AND** the system SHALL return the updated payment with status 200 OK

#### Scenario: Update payment status from PENDING to FAILED
- **WHEN** a client sends a PATCH request to `/payments/{id}/status` with status `FAILED`
- **AND** the payment exists and has status `PENDING`
- **THEN** the system SHALL update the payment status to `FAILED`
- **AND** the system SHALL update the `updatedAt` timestamp
- **AND** the system SHALL return the updated payment with status 200 OK

#### Scenario: Update payment status from PENDING to CANCELED
- **WHEN** a client sends a PATCH request to `/payments/{id}/status` with status `CANCELED`
- **AND** the payment exists and has status `PENDING`
- **THEN** the system SHALL update the payment status to `CANCELED`
- **AND** the system SHALL update the `updatedAt` timestamp
- **AND** the system SHALL return the updated payment with status 200 OK

#### Scenario: Reject status update for non-PENDING payment
- **WHEN** a client sends a PATCH request to `/payments/{id}/status` with a new status
- **AND** the payment exists but has status `APPROVED`, `FAILED`, or `CANCELED`
- **THEN** the system SHALL reject the status update
- **AND** the system SHALL return status 409 Conflict
- **AND** the system SHALL return an ErrorResponse with code 409 and an appropriate error message indicating the invalid transition
- **AND** the payment status SHALL remain unchanged

#### Scenario: Reject status update for non-existent payment
- **WHEN** a client sends a PATCH request to `/payments/{id}/status` with a payment ID that does not exist
- **THEN** the system SHALL return status 404 Not Found
- **AND** the system SHALL return an ErrorResponse with code 404 and an appropriate error message

#### Scenario: Reject invalid status value
- **WHEN** a client sends a PATCH request to `/payments/{id}/status` with an invalid status value
- **THEN** the system SHALL return status 400 Bad Request
- **AND** the system SHALL return an ErrorResponse with code 400 and validation error details

