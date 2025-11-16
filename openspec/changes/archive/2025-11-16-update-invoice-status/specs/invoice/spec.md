## ADDED Requirements

### Requirement: Invoice Status Update
The system SHALL provide an API endpoint to update the status of an existing invoice. The endpoint SHALL validate status transitions according to business rules, update relevant date fields when status changes, and return the updated invoice. Only valid transitions SHALL be allowed: CREATED → SENT, CREATED → PAID, CREATED → CANCELLED, and SENT → PAID. Terminal states (PAID, CANCELLED) SHALL NOT be allowed to change status. Backward transitions (e.g., PAID → SENT, SENT → CREATED) SHALL be rejected.

#### Scenario: Update invoice status from CREATED to SENT
- **WHEN** a client sends a PATCH request to `/invoices/{id}/status` with status `SENT`
- **AND** the invoice exists and has status `CREATED`
- **THEN** the system SHALL update the invoice status to `SENT`
- **AND** the system SHALL set `sentAt` date to current date if the field exists
- **AND** the system SHALL return the updated invoice with status 200 OK

#### Scenario: Update invoice status from CREATED to PAID
- **WHEN** a client sends a PATCH request to `/invoices/{id}/status` with status `PAID`
- **AND** the invoice exists and has status `CREATED`
- **THEN** the system SHALL update the invoice status to `PAID`
- **AND** the system SHALL set `paidAt` date to current date if the field exists
- **AND** the system SHALL return the updated invoice with status 200 OK

#### Scenario: Update invoice status from CREATED to CANCELLED
- **WHEN** a client sends a PATCH request to `/invoices/{id}/status` with status `CANCELLED`
- **AND** the invoice exists and has status `CREATED`
- **THEN** the system SHALL update the invoice status to `CANCELLED`
- **AND** the system SHALL set `cancelledAt` date to current date if the field exists
- **AND** the system SHALL return the updated invoice with status 200 OK

#### Scenario: Update invoice status from SENT to PAID
- **WHEN** a client sends a PATCH request to `/invoices/{id}/status` with status `PAID`
- **AND** the invoice exists and has status `SENT`
- **THEN** the system SHALL update the invoice status to `PAID`
- **AND** the system SHALL set `paidAt` date to current date if the field exists
- **AND** the system SHALL return the updated invoice with status 200 OK

#### Scenario: Reject backward transition from SENT to CREATED
- **WHEN** a client sends a PATCH request to `/invoices/{id}/status` with status `CREATED`
- **AND** the invoice exists but has status `SENT`
- **THEN** the system SHALL reject the status update
- **AND** the system SHALL return status 409 Conflict
- **AND** the system SHALL return an ErrorResponse with code 409 and an appropriate error message indicating the invalid backward transition
- **AND** the invoice status SHALL remain unchanged

#### Scenario: Reject backward transition from PAID to SENT
- **WHEN** a client sends a PATCH request to `/invoices/{id}/status` with status `SENT`
- **AND** the invoice exists but has status `PAID`
- **THEN** the system SHALL reject the status update
- **AND** the system SHALL return status 409 Conflict
- **AND** the system SHALL return an ErrorResponse with code 409 and an appropriate error message indicating the invalid backward transition
- **AND** the invoice status SHALL remain unchanged

#### Scenario: Reject status update from terminal state PAID
- **WHEN** a client sends a PATCH request to `/invoices/{id}/status` with a new status
- **AND** the invoice exists but has status `PAID`
- **THEN** the system SHALL reject the status update
- **AND** the system SHALL return status 409 Conflict
- **AND** the system SHALL return an ErrorResponse with code 409 and an appropriate error message indicating that invoices in PAID status cannot be modified
- **AND** the invoice status SHALL remain unchanged

#### Scenario: Reject status update from terminal state CANCELLED
- **WHEN** a client sends a PATCH request to `/invoices/{id}/status` with a new status
- **AND** the invoice exists but has status `CANCELLED`
- **THEN** the system SHALL reject the status update
- **AND** the system SHALL return status 409 Conflict
- **AND** the system SHALL return an ErrorResponse with code 409 and an appropriate error message indicating that invoices in CANCELLED status cannot be modified
- **AND** the invoice status SHALL remain unchanged

#### Scenario: Reject status update for non-existent invoice
- **WHEN** a client sends a PATCH request to `/invoices/{id}/status` with an invoice ID that does not exist
- **THEN** the system SHALL return status 404 Not Found
- **AND** the system SHALL return an ErrorResponse with code 404 and an appropriate error message

#### Scenario: Reject invalid status value
- **WHEN** a client sends a PATCH request to `/invoices/{id}/status` with an invalid status value
- **THEN** the system SHALL return status 400 Bad Request
- **AND** the system SHALL return an ErrorResponse with code 400 and validation error details

### Requirement: Invoice Status Transition Validation
The system SHALL validate all invoice status transitions according to business rules and reject invalid transitions with clear error responses. The validation SHALL be centralized in the business layer and SHALL enforce that only valid transitions are allowed: CREATED → SENT, CREATED → PAID, CREATED → CANCELLED, and SENT → PAID. No status changes SHALL be allowed from terminal states (PAID, CANCELLED) to any other state. Backward transitions SHALL be rejected. The system SHALL also reject transitions to unknown or invalid status values.

#### Scenario: Valid transition from CREATED to SENT
- **WHEN** a client attempts to update an invoice status from CREATED to SENT
- **THEN** the system SHALL allow the transition
- **AND** the status update SHALL proceed normally

#### Scenario: Valid transition from CREATED to PAID
- **WHEN** a client attempts to update an invoice status from CREATED to PAID
- **THEN** the system SHALL allow the transition
- **AND** the status update SHALL proceed normally

#### Scenario: Valid transition from CREATED to CANCELLED
- **WHEN** a client attempts to update an invoice status from CREATED to CANCELLED
- **THEN** the system SHALL allow the transition
- **AND** the status update SHALL proceed normally

#### Scenario: Valid transition from SENT to PAID
- **WHEN** a client attempts to update an invoice status from SENT to PAID
- **THEN** the system SHALL allow the transition
- **AND** the status update SHALL proceed normally

#### Scenario: Invalid transition from PAID to SENT
- **WHEN** a client attempts to update an invoice status from PAID to SENT
- **THEN** the system SHALL reject the transition
- **AND** the system SHALL return status 409 Conflict
- **AND** the response SHALL include an ErrorResponse with code 409 and a message indicating the invalid backward transition
- **AND** the system SHALL log the business rule violation with invoice ID, current status, and target status

#### Scenario: Invalid transition from SENT to CREATED
- **WHEN** a client attempts to update an invoice status from SENT to CREATED
- **THEN** the system SHALL reject the transition
- **AND** the system SHALL return status 409 Conflict
- **AND** the response SHALL include an ErrorResponse with code 409 and a message indicating the invalid backward transition
- **AND** the system SHALL log the business rule violation

#### Scenario: Invalid transition from PAID to CANCELLED
- **WHEN** a client attempts to update an invoice status from PAID to CANCELLED
- **THEN** the system SHALL reject the transition
- **AND** the system SHALL return status 409 Conflict
- **AND** the response SHALL include an ErrorResponse with code 409 and a message indicating that invoices in PAID status cannot be modified
- **AND** the system SHALL log the business rule violation

#### Scenario: Invalid transition from CANCELLED to PAID
- **WHEN** a client attempts to update an invoice status from CANCELLED to PAID
- **THEN** the system SHALL reject the transition
- **AND** the system SHALL return status 409 Conflict
- **AND** the response SHALL include an ErrorResponse with code 409 and a message indicating that invoices in CANCELLED status cannot be modified
- **AND** the system SHALL log the business rule violation

#### Scenario: Invalid target status value
- **WHEN** a client attempts to update an invoice status to an unknown or invalid status value
- **THEN** the system SHALL reject the transition
- **AND** the system SHALL return status 409 Conflict
- **AND** the response SHALL include an ErrorResponse with code 409 and a message indicating the invalid status value
- **AND** the system SHALL log the business rule violation

