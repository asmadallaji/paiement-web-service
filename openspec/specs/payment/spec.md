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

