## Why

When a payment is approved, the system needs to create an invoice to maintain legal and accounting records. This enables proper financial tracking and provides customers with official documentation of their transactions. Automating invoice creation from approved payments eliminates manual work and ensures no approved payment goes without an invoice.

## What Changes

- **ADDED**: Invoice entity model with fields: id, invoiceNumber, paymentId, userId, amount, currency, status, issueDate, dueDate (optional), orderId
- **ADDED**: Invoice status enum (CREATED, SENT, PAID, CANCELLED)
- **ADDED**: Invoice repository for database persistence
- **ADDED**: Invoice service layer with business logic for invoice creation
- **ADDED**: Unique invoice number generation (timestamp + sequence)
- **MODIFIED**: Payment status update logic to automatically create invoice when payment transitions to APPROVED
- **ADDED**: Invoice creation API endpoint (`POST /invoices`) for manual creation if needed
- **ADDED**: Invoice retrieval API endpoint (`GET /invoices/{id}`)
- **ADDED**: Invoice retrieval by payment ID (`GET /invoices?paymentId={id}`)

## Impact

- **Affected specs**: 
  - New capability `invoice` (invoice creation and management)
  - Modified capability `payment` (adds invoice creation trigger on APPROVED status)
- **Affected code**: 
  - New Invoice entity (`com.asma.paymentservice.entity.Invoice`)
  - New InvoiceStatus enum (`com.asma.paymentservice.entity.InvoiceStatus`)
  - New InvoiceRepository (`com.asma.paymentservice.repository.InvoiceRepository`)
  - New InvoiceService (`com.asma.paymentservice.service.InvoiceService`)
  - Modified PaymentService (`com.asma.paymentservice.service.PaymentService`) - add invoice creation on APPROVED
  - Generated InvoiceController (from OpenAPI spec)
  - Generated Invoice DTOs (from OpenAPI spec)
- **Database**: New `invoice` table schema

