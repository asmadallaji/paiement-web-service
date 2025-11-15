## ADDED Requirements

### Requirement: Payment Creation
The system SHALL provide an API endpoint to create a new payment with required and optional fields, validate input data according to business rules, initialize the payment with appropriate default values, persist it to the database, and return the created payment with its unique identifier.

#### Scenario: Successful payment creation with all required fields
- **WHEN** a client sends a valid payment creation request with `amount` > 0, non-empty `currency`, valid `method`, and non-empty `userId`
- **THEN** the system SHALL create a payment with status `PENDING`, set `createdAt` and `updatedAt` timestamps, save it to the database, and return the payment with a generated unique identifier

#### Scenario: Successful payment creation with optional orderId
- **WHEN** a client sends a valid payment creation request including an optional `orderId`
- **THEN** the system SHALL create the payment with the provided `orderId` and return the complete payment information

#### Scenario: Payment creation with invalid amount
- **WHEN** a client sends a payment creation request with `amount` <= 0
- **THEN** the system SHALL reject the request and return a 400 Bad Request error with an appropriate validation message

#### Scenario: Payment creation with empty currency
- **WHEN** a client sends a payment creation request with an empty or null `currency`
- **THEN** the system SHALL reject the request and return a 400 Bad Request error with an appropriate validation message

#### Scenario: Payment creation with invalid payment method
- **WHEN** a client sends a payment creation request with a `method` that is not in the list of allowed payment methods
- **THEN** the system SHALL reject the request and return a 400 Bad Request error with an appropriate validation message

#### Scenario: Payment creation with missing userId
- **WHEN** a client sends a payment creation request without a `userId` or with an empty/null `userId`
- **THEN** the system SHALL reject the request and return a 400 Bad Request error with an appropriate validation message

#### Scenario: Payment initialization with default status
- **WHEN** a payment is successfully created
- **THEN** the payment SHALL have status `PENDING` regardless of any status value provided in the request

#### Scenario: Payment timestamp initialization
- **WHEN** a payment is successfully created
- **THEN** the payment SHALL have `createdAt` and `updatedAt` timestamps set to the current date and time

