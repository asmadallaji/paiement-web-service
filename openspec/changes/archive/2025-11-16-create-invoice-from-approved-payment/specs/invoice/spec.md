## ADDED Requirements

### Requirement: Invoice Creation from Approved Payment
The system SHALL automatically create an invoice when a payment status transitions to APPROVED. The invoice SHALL be created with status CREATED and SHALL include all relevant payment information. The system SHALL generate a unique invoice number for each invoice and SHALL persist the invoice to the database.

#### Scenario: Automatic invoice creation when payment is approved
- **WHEN** a payment status is updated from PENDING to APPROVED
- **THEN** the system SHALL automatically create a new invoice
- **AND** the invoice SHALL have status CREATED
- **AND** the invoice SHALL include: paymentId, userId, amount, currency, orderId (if present in payment), issueDate set to current date
- **AND** the invoice SHALL have a unique invoiceNumber generated
- **AND** the invoice SHALL be persisted to the database
- **AND** the invoice creation SHALL be logged

#### Scenario: Invoice creation with all payment fields
- **WHEN** an invoice is created from an approved payment
- **THEN** the invoice SHALL contain paymentId referencing the approved payment
- **AND** the invoice SHALL contain userId from the payment
- **AND** the invoice SHALL contain amount matching the payment amount
- **AND** the invoice SHALL contain currency matching the payment currency
- **AND** the invoice SHALL contain orderId if the payment has an orderId
- **AND** the invoice SHALL have issueDate set to the current date
- **AND** the invoice SHALL have dueDate as optional (null if not specified)

#### Scenario: Unique invoice number generation
- **WHEN** an invoice is created
- **THEN** the system SHALL generate a unique invoiceNumber
- **AND** the invoiceNumber SHALL be unique across all invoices
- **AND** the invoiceNumber SHALL be formatted as a string (e.g., timestamp-based with sequence)

### Requirement: Manual Invoice Creation
The system SHALL provide an API endpoint to manually create an invoice from an approved payment. This endpoint SHALL allow creating invoices for payments that were approved before invoice creation was implemented, or for cases where automatic creation failed.

#### Scenario: Manual invoice creation via API
- **WHEN** a client sends a POST request to `/invoices` with a paymentId
- **AND** the payment exists and has status APPROVED
- **AND** no invoice already exists for this payment
- **THEN** the system SHALL create a new invoice with status CREATED
- **AND** the system SHALL return the created invoice with status 201 Created
- **AND** the invoice SHALL include all payment information

#### Scenario: Manual invoice creation for non-approved payment
- **WHEN** a client sends a POST request to `/invoices` with a paymentId
- **AND** the payment exists but does not have status APPROVED
- **THEN** the system SHALL reject the request
- **AND** the system SHALL return status 409 Conflict
- **AND** the response SHALL include an error message indicating that invoices can only be created for approved payments

#### Scenario: Manual invoice creation for duplicate invoice
- **WHEN** a client sends a POST request to `/invoices` with a paymentId
- **AND** an invoice already exists for this payment
- **THEN** the system SHALL reject the request
- **AND** the system SHALL return status 409 Conflict
- **AND** the response SHALL include an error message indicating that an invoice already exists for this payment

### Requirement: Invoice Retrieval
The system SHALL provide API endpoints to retrieve invoices by ID or by payment ID. The endpoints SHALL return all invoice details including invoice number, payment reference, amounts, dates, and status.

#### Scenario: Retrieve invoice by ID
- **WHEN** a client sends a GET request to `/invoices/{id}` with a valid invoice ID
- **AND** the invoice exists in the database
- **THEN** the system SHALL return the invoice details with status 200 OK
- **AND** the response SHALL include all invoice fields: id, invoiceNumber, paymentId, userId, amount, currency, status, issueDate, dueDate, orderId

#### Scenario: Retrieve non-existent invoice by ID
- **WHEN** a client sends a GET request to `/invoices/{id}` with an invoice ID that does not exist
- **THEN** the system SHALL return status 404 Not Found
- **AND** the response SHALL include an ErrorResponse with code 404 and an appropriate error message

#### Scenario: Retrieve invoice by payment ID
- **WHEN** a client sends a GET request to `/invoices?paymentId={paymentId}` with a valid payment ID
- **AND** an invoice exists for this payment
- **THEN** the system SHALL return the invoice details with status 200 OK
- **AND** the response SHALL include all invoice fields

#### Scenario: Retrieve invoice by payment ID when no invoice exists
- **WHEN** a client sends a GET request to `/invoices?paymentId={paymentId}` with a valid payment ID
- **AND** no invoice exists for this payment
- **THEN** the system SHALL return status 404 Not Found
- **AND** the response SHALL include an ErrorResponse with code 404 and an appropriate error message

