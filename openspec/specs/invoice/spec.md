# invoice Specification

## Purpose
The invoice capability enables the creation, retrieval, and management of invoices for approved payments. Invoices provide a complete record of billing information including amounts, currency, dates, status, and payment references. This capability supports accounting, legal, and administrative requirements by allowing users to consult invoice details (Story C2) and track the invoice lifecycle through status updates.
## Requirements
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

### Requirement: Invoice Retrieval (Story C2)
The system SHALL provide API endpoints to retrieve invoices by ID or by payment ID. The endpoints SHALL return all invoice details required for consultation purposes (Story C2), including: montant (amount), devise (currency), dates (issueDate, dueDate, and status-specific dates), statut (status), and références paiement (paymentId). This capability enables users and administrators to consult invoice details for accounting, legal, and administrative purposes.

#### Scenario: Retrieve invoice by ID
- **WHEN** a client sends a GET request to `/invoices/{id}` with a valid invoice ID
- **AND** the invoice exists in the database
- **THEN** the system SHALL return the invoice details with status 200 OK
- **AND** the response SHALL include all invoice fields: id, invoiceNumber, paymentId, userId, amount, currency, status, issueDate, dueDate, orderId
- **AND** the response SHALL include montant (amount), devise (currency), dates (issueDate, dueDate), statut (status), and références paiement (paymentId)

#### Scenario: Retrieve non-existent invoice by ID
- **WHEN** a client sends a GET request to `/invoices/{id}` with an invoice ID that does not exist
- **THEN** the system SHALL return status 404 Not Found
- **AND** the response SHALL include an ErrorResponse with code 404 and an appropriate error message

#### Scenario: Retrieve invoice by payment ID
- **WHEN** a client sends a GET request to `/invoices?paymentId={paymentId}` with a valid payment ID
- **AND** an invoice exists for this payment
- **THEN** the system SHALL return the invoice details with status 200 OK
- **AND** the response SHALL include all invoice fields: id, invoiceNumber, paymentId, userId, amount, currency, status, issueDate, dueDate, orderId
- **AND** the response SHALL include montant (amount), devise (currency), dates (issueDate, dueDate), statut (status), and références paiement (paymentId)

#### Scenario: Retrieve invoice by payment ID when no invoice exists
- **WHEN** a client sends a GET request to `/invoices?paymentId={paymentId}` with a valid payment ID
- **AND** no invoice exists for this payment
- **THEN** the system SHALL return status 404 Not Found
- **AND** the response SHALL include an ErrorResponse with code 404 and an appropriate error message

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

### Requirement: Invoice Listing with Filters and Pagination
The system SHALL provide an API endpoint to list invoices with optional filtering by userId, status, and issue date period, and support pagination. The endpoint SHALL return a paginated list of invoices with metadata including total count, total pages, current page, and page size. All filter parameters are optional and can be combined. Pagination parameters have default values and maximum limits. Results SHALL be sorted by issueDate in descending order (most recent first).

#### Scenario: List all invoices without filters
- **WHEN** a client sends a GET request to `/invoices` without any query parameters
- **THEN** the system SHALL return a paginated list of all invoices with status 200 OK
- **AND** the response SHALL include pagination metadata (totalElements, totalPages, page, size)
- **AND** the default page size SHALL be 20
- **AND** the default page SHALL be 0 (first page)
- **AND** invoices SHALL be sorted by issueDate in descending order (most recent first)

#### Scenario: List invoices filtered by status
- **WHEN** a client sends a GET request to `/invoices?status=CREATED`
- **THEN** the system SHALL return only invoices with status CREATED
- **AND** the response SHALL include pagination metadata
- **AND** the totalElements SHALL reflect the count of filtered invoices

#### Scenario: List invoices filtered by userId
- **WHEN** a client sends a GET request to `/invoices?userId=user123`
- **THEN** the system SHALL return only invoices for the specified userId
- **AND** the response SHALL include pagination metadata
- **AND** the totalElements SHALL reflect the count of filtered invoices

#### Scenario: List invoices filtered by date period
- **WHEN** a client sends a GET request to `/invoices?fromDate=2024-01-01&toDate=2024-01-31`
- **THEN** the system SHALL return only invoices with issueDate between fromDate and toDate (inclusive)
- **AND** the response SHALL include pagination metadata
- **AND** the totalElements SHALL reflect the count of filtered invoices

#### Scenario: List invoices with multiple filters
- **WHEN** a client sends a GET request to `/invoices?status=CREATED&userId=user123`
- **THEN** the system SHALL return only invoices matching all specified filters
- **AND** the response SHALL include pagination metadata
- **AND** the totalElements SHALL reflect the count of invoices matching all filters

#### Scenario: List invoices with pagination
- **WHEN** a client sends a GET request to `/invoices?page=0&size=10`
- **THEN** the system SHALL return the first 10 invoices (page 0)
- **AND** the response SHALL include pagination metadata with page=0, size=10
- **AND** the totalPages SHALL be calculated based on totalElements and size

#### Scenario: Navigate to next page
- **WHEN** a client sends a GET request to `/invoices?page=1&size=10`
- **THEN** the system SHALL return invoices 11-20 (second page)
- **AND** the response SHALL include pagination metadata with page=1, size=10

#### Scenario: Invalid pagination parameters
- **WHEN** a client sends a GET request to `/invoices?page=-1`
- **OR** a client sends a GET request to `/invoices?size=0`
- **OR** a client sends a GET request to `/invoices?size=200`
- **THEN** the system SHALL return status 400 Bad Request
- **AND** the response SHALL include an ErrorResponse with code 400 and validation error details
- **AND** page SHALL be >= 0
- **AND** size SHALL be between 1 and 100 (inclusive)

#### Scenario: Invalid date range
- **WHEN** a client sends a GET request to `/invoices?fromDate=2024-01-31&toDate=2024-01-01`
- **AND** fromDate is after toDate
- **THEN** the system SHALL return status 400 Bad Request
- **AND** the response SHALL include an ErrorResponse with code 400 and validation error details indicating invalid date range

#### Scenario: Empty result set
- **WHEN** a client sends a GET request to `/invoices?status=PAID`
- **AND** no invoices exist with status PAID
- **THEN** the system SHALL return status 200 OK
- **AND** the response SHALL include an empty content array
- **AND** totalElements SHALL be 0
- **AND** totalPages SHALL be 0

