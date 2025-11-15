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

