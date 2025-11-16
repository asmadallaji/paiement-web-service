## 1. Validation
- [x] 1.1 Verify that GET /invoices/{id} endpoint is implemented and working
- [x] 1.2 Verify that GET /invoices?paymentId={paymentId} endpoint is implemented and working
- [x] 1.3 Verify that 404 responses are returned when invoice is not found
- [x] 1.4 Verify that all required fields are included in invoice response (montant, devise, dates, statut, références paiement)

## 2. Specification Review
- [x] 2.1 Review existing "Invoice Retrieval" requirement in invoice spec
- [x] 2.2 Ensure all Story C2 scenarios are covered
- [x] 2.3 Update invoice spec Purpose section if needed

## 3. Testing
- [x] 3.1 Review existing unit tests for invoice retrieval
- [x] 3.2 Add integration tests for GET /invoices/{id} endpoint if missing
- [x] 3.3 Add integration tests for GET /invoices?paymentId={paymentId} endpoint if missing
- [x] 3.4 Verify 404 error handling in integration tests

## 4. Documentation
- [x] 4.1 Mark Story C2 tasks as complete in Backlog.md
- [x] 4.2 Ensure OpenAPI spec documents invoice retrieval endpoints clearly

