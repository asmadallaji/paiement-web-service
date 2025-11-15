## MODIFIED Requirements

### Requirement: Payment Creation
The system SHALL provide an API endpoint to create a new payment with required and optional fields, validate input data according to business rules, initialize the payment with appropriate default values, persist it to the database, and return the created payment with its unique identifier. The system SHALL correctly deserialize JSON request bodies containing optional fields using `JsonNullable`.

#### Scenario: Successful payment creation with optional orderId
- **WHEN** a client sends a valid payment creation request including an optional `orderId` field in JSON format
- **THEN** the system SHALL correctly deserialize the `orderId` field using `JsonNullable`
- **AND** the system SHALL create the payment with the provided `orderId`
- **AND** the system SHALL return the complete payment information without errors

#### Scenario: Payment creation deserialization error
- **WHEN** a client sends a payment creation request with an optional field using `JsonNullable`
- **AND** Jackson is not configured to handle `JsonNullable` deserialization
- **THEN** the system SHALL return a 500 Internal Server Error
- **AND** after configuring Jackson with `JsonNullableModule`, the system SHALL successfully deserialize and process the request

