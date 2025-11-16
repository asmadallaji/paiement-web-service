## Why

The finance team and system need to update invoice status to reflect the real state of invoices (sent, paid, cancelled). This enables proper tracking of the invoice lifecycle and ensures that invoice status accurately reflects business operations. Status transitions must follow logical business rules to prevent invalid state changes and maintain data integrity.

## What Changes

- **ADDED**: Invoice status update API endpoint (`PATCH /invoices/{id}/status`)
- **ADDED**: Status transition validation logic enforcing allowed transitions: CREATED → SENT → PAID, CREATED → CANCELLED
- **ADDED**: Prevention of backward transitions (e.g., PAID → SENT, SENT → CREATED)
- **ADDED**: Prevention of transitions from terminal states (PAID, CANCELLED cannot be changed)
- **ADDED**: Optional date tracking fields (sentAt, paidAt, cancelledAt) to Invoice entity for audit purposes
- **ADDED**: Service layer method to validate and update invoice status
- **ADDED**: Exception handling for invalid status transitions

## Impact

- **Affected specs**: 
  - Modified capability `invoice` (adds invoice status update requirement)
- **Affected code**: 
  - Modified Invoice entity (add optional date fields: sentAt, paidAt, cancelledAt)
  - Modified InvoiceService (add updateInvoiceStatus and validateStatusTransition methods)
  - Modified InvoiceController (add updateInvoiceStatus endpoint)
  - Updated OpenAPI specification (add PATCH /invoices/{id}/status endpoint)
  - Generated UpdateInvoiceStatusRequest DTO (from OpenAPI spec)
- **Database**: Modified `invoice` table schema (add sentAt, paidAt, cancelledAt columns, nullable)

