## MODIFIED Requirements

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

