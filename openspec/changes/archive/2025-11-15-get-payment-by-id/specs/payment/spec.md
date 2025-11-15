## ADDED Requirements

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

