## Context

The Payment Service currently handles payment creation, retrieval, listing, and status updates. When a payment is approved, there is a business need to create an invoice for legal and accounting purposes. This change introduces invoice capability to the Payment Service, automatically creating invoices when payments transition to APPROVED status.

## Goals / Non-Goals

### Goals
- Automatically create invoices when payments are approved
- Maintain referential integrity between payments and invoices (one invoice per approved payment)
- Generate unique invoice numbers for each invoice
- Provide API endpoints for manual invoice creation and retrieval
- Ensure invoice creation doesn't break payment status update flow

### Non-Goals
- Invoice status management (SENT, PAID, CANCELLED) - this will be handled in future stories
- Invoice listing with filters - this will be handled in Story C3
- Invoice status updates - this will be handled in Story C4
- Integration with external BillingService - this is handled in EPIC D (separate concern)
- Invoice PDF generation or email sending

## Decisions

### Decision: Invoice Creation in Same Transaction as Payment Update
**What**: When a payment status is updated to APPROVED, the invoice creation happens in the same database transaction.

**Why**: This ensures data consistency. If invoice creation fails, the payment status update should also roll back. However, we need to be careful not to block payment approval if invoice creation has transient issues.

**Alternatives considered**:
- Separate transaction: Could lead to inconsistent state if payment is approved but invoice creation fails
- Async processing: Adds complexity and eventual consistency concerns

### Decision: Invoice Number Generation Strategy
**What**: Generate invoice numbers using a timestamp-based format with sequence (e.g., `INV-YYYYMMDD-HHMMSS-{sequence}` or `INV-{timestamp}-{random}`).

**Why**: Ensures uniqueness and provides human-readable invoice numbers. The sequence component handles cases where multiple invoices are created in the same second.

**Alternatives considered**:
- UUID: Guaranteed unique but not human-readable
- Database sequence: Requires additional database object, simpler but less flexible

### Decision: One Invoice Per Payment
**What**: Each approved payment can have at most one invoice. If an invoice already exists for a payment, creation attempts are rejected.

**Why**: Maintains referential integrity and prevents duplicate invoices for the same payment.

**Alternatives considered**:
- Multiple invoices per payment: Could lead to confusion and accounting issues
- Allow overwriting: Could lose audit trail

### Decision: Invoice Creation Only for APPROVED Payments
**What**: Invoices are only created automatically when payment status transitions to APPROVED. Manual creation also requires the payment to be APPROVED.

**Why**: Aligns with business requirement - invoices should only be created for approved payments.

**Alternatives considered**:
- Create invoices for all payments: Doesn't match business requirement
- Create invoices on payment creation: Payment might not be approved, leading to invalid invoices

## Risks / Trade-offs

### Risk: Invoice Creation Failure Blocks Payment Approval
**Mitigation**: Invoice creation errors are logged but don't fail the payment status update. However, this could lead to approved payments without invoices. A manual retry mechanism is provided via the POST /invoices endpoint.

### Risk: Invoice Number Collision
**Mitigation**: Use timestamp + sequence or database-level unique constraint to ensure uniqueness. Add retry logic if collision occurs.

### Risk: Performance Impact on Payment Status Update
**Mitigation**: Invoice creation is lightweight (single database insert). If performance becomes an issue, consider async processing in the future.

## Migration Plan

1. Deploy database schema changes (invoice table)
2. Deploy code changes (invoice entity, service, controller)
3. Existing approved payments will not have invoices - manual creation via API can be used
4. New approved payments will automatically get invoices

## Open Questions

- Should we add a flag to Payment entity to track if invoice was created? (Considered but not needed - can query invoice table)
- Should invoice creation be idempotent? (Yes - check if invoice exists before creating)
- What happens if payment is deleted? (Cascade delete or prevent deletion - to be decided in future)

