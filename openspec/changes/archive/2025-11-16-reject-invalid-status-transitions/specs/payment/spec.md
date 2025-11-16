## ADDED Requirements

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

