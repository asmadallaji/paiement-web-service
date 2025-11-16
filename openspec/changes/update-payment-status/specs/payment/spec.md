## ADDED Requirements

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

