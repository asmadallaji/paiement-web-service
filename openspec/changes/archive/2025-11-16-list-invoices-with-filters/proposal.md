## Why

Administrators need to list invoices with filtering capabilities to track billing activity globally. This enables them to view all invoices, filter by user, status, or creation period, and navigate through results using pagination. This capability is essential for financial oversight, reporting, and administrative management of the billing system.

## What Changes

- **ADDED**: Invoice listing API endpoint (`GET /invoices`) with optional filters and pagination
- **ADDED**: Filtering by `userId` to list invoices for a specific user
- **ADDED**: Filtering by `status` to list invoices by status (CREATED, SENT, PAID, CANCELLED)
- **ADDED**: Filtering by period (date range) using `fromDate` and `toDate` query parameters based on `issueDate`
- **ADDED**: Pagination support with default page size of 20 and maximum of 100
- **ADDED**: Invoice list response schema (`InvoiceListResponse`) with pagination metadata
- **ADDED**: Repository methods for filtered invoice queries with pagination
- **ADDED**: Service layer method to handle invoice listing with filters and pagination

## Impact

- **Affected specs**: 
  - Modified capability `invoice` (adds invoice listing requirement)
- **Affected code**: 
  - Modified InvoiceRepository (add filtered query methods)
  - Modified InvoiceService (add listInvoices method)
  - Modified InvoiceController (add listInvoices endpoint)
  - Updated OpenAPI specification (add GET /invoices endpoint with filters)
  - Generated InvoiceListResponse DTO (from OpenAPI spec)
- **Database**: No schema changes (uses existing invoice table)

