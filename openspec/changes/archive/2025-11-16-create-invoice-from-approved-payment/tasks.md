## 1. Domain Model
- [x] 1.1 Create `InvoiceStatus` enum with values: CREATED, SENT, PAID, CANCELLED
- [x] 1.2 Create `Invoice` entity class with JPA annotations
- [x] 1.3 Define fields: `id` (Long), `invoiceNumber` (String, unique), `paymentId` (Long, nullable false), `userId` (String), `amount` (BigDecimal), `currency` (String), `status` (InvoiceStatus enum), `issueDate` (LocalDate), `dueDate` (LocalDate, nullable), `orderId` (String, nullable)
- [x] 1.4 Configure JPA entity with appropriate annotations (@Entity, @Id, @Column, @Enumerated for status, @UniqueConstraint for invoiceNumber)
- [x] 1.5 Add Lombok annotations (@Getter, @Setter, @Builder, @NoArgsConstructor, @AllArgsConstructor)

## 2. Repository Layer
- [x] 2.1 Create `InvoiceRepository` interface extending `JpaRepository<Invoice, Long>`
- [x] 2.2 Add method `Optional<Invoice> findByPaymentId(Long paymentId)` to find invoice by payment ID
- [x] 2.3 Add method to check if invoice exists for a payment: `boolean existsByPaymentId(Long paymentId)`

## 3. Service Layer - Invoice Service
- [x] 3.1 Create `InvoiceService` class with invoice creation and retrieval methods
- [x] 3.2 Implement `createInvoiceFromPayment(Payment payment)` method:
  - [x] 3.2.1 Check if invoice already exists for this payment (prevent duplicates)
  - [x] 3.2.2 Generate unique invoice number (e.g., timestamp-based format: `INV-YYYYMMDD-HHMMSS-{sequence}`)
  - [x] 3.2.3 Create invoice entity with status CREATED
  - [x] 3.2.4 Copy payment information: paymentId, userId, amount, currency, orderId
  - [x] 3.2.5 Set issueDate to current date
  - [x] 3.2.6 Save invoice to database
  - [x] 3.2.7 Log invoice creation
- [x] 3.3 Implement `getInvoiceById(Long id)` method with 404 handling
- [x] 3.4 Implement `getInvoiceByPaymentId(Long paymentId)` method with 404 handling
- [x] 3.5 Implement `createInvoiceManually(Long paymentId)` method:
  - [x] 3.5.1 Validate payment exists and has status APPROVED
  - [x] 3.5.2 Check if invoice already exists (return 409 if exists)
  - [x] 3.5.3 Create invoice using `createInvoiceFromPayment`
- [x] 3.6 Implement invoice number generation logic ensuring uniqueness

## 4. Service Layer - Payment Service Integration
- [x] 4.1 Modify `PaymentService.updatePaymentStatus` method:
  - [x] 4.1.1 Inject `InvoiceService` dependency
  - [x] 4.1.2 After successful status update to APPROVED, call `InvoiceService.createInvoiceFromPayment`
  - [x] 4.1.3 Handle invoice creation errors gracefully (log but don't fail payment update)
  - [x] 4.1.4 Ensure transaction boundaries are correct (invoice creation in same transaction)

## 5. OpenAPI Specification
- [x] 5.1 Create or update OpenAPI specification file to include invoice endpoints
- [x] 5.2 Define `CreateInvoiceRequest` schema with `paymentId` field
- [x] 5.3 Define `InvoiceResponse` schema with all invoice fields: id, invoiceNumber, paymentId, userId, amount, currency, status, issueDate, dueDate, orderId
- [x] 5.4 Define `InvoiceStatus` enum in OpenAPI spec (CREATED, SENT, PAID, CANCELLED)
- [x] 5.5 Define `POST /invoices` endpoint for manual invoice creation
- [x] 5.6 Define `GET /invoices/{id}` endpoint for invoice retrieval by ID
- [x] 5.7 Define `GET /invoices?paymentId={paymentId}` endpoint for invoice retrieval by payment ID
- [x] 5.8 Define error responses (400, 404, 409, 500)

## 6. Code Generation
- [x] 6.1 Run OpenAPI Generator to generate Invoice DTOs and controller interfaces
- [x] 6.2 Verify generated code is in correct package structure

## 7. Controller Implementation
- [x] 7.1 Implement `InvoiceController` class implementing generated `InvoicesApi` interface
- [x] 7.2 Implement `POST /invoices` endpoint calling `InvoiceService.createInvoiceManually`
- [x] 7.3 Implement `GET /invoices/{id}` endpoint calling `InvoiceService.getInvoiceById`
- [x] 7.4 Implement `GET /invoices?paymentId={paymentId}` endpoint calling `InvoiceService.getInvoiceByPaymentId`
- [x] 7.5 Add proper exception handling and error responses

## 8. Database Migration
- [ ] 8.1 Create database migration script or update schema for `invoice` table
- [ ] 8.2 Ensure unique constraint on `invoiceNumber` column
- [ ] 8.3 Ensure foreign key relationship to `payment` table (paymentId)
- [ ] 8.4 Add indexes if needed for performance (paymentId lookup)

## 9. Testing
- [x] 9.1 Write unit tests for `InvoiceService`:
  - [x] 9.1.1 Test `createInvoiceFromPayment` with valid payment
  - [x] 9.1.2 Test duplicate invoice prevention
  - [x] 9.1.3 Test invoice number generation uniqueness
  - [x] 9.1.4 Test `getInvoiceById` with existing and non-existing invoices
  - [x] 9.1.5 Test `getInvoiceByPaymentId` with existing and non-existing invoices
  - [x] 9.1.6 Test `createInvoiceManually` with approved payment
  - [x] 9.1.7 Test `createInvoiceManually` with non-approved payment (should fail)
  - [x] 9.1.8 Test `createInvoiceManually` with duplicate invoice (should fail)
- [ ] 9.2 Write integration tests for `InvoiceController`:
  - [ ] 9.2.1 Test POST /invoices endpoint
  - [ ] 9.2.2 Test GET /invoices/{id} endpoint
  - [ ] 9.2.3 Test GET /invoices?paymentId={paymentId} endpoint
  - [ ] 9.2.4 Test error cases (404, 409, 400)
- [ ] 9.3 Write integration tests for automatic invoice creation:
  - [ ] 9.3.1 Test that invoice is created when payment status changes to APPROVED
  - [ ] 9.3.2 Test that invoice is NOT created when payment status changes to FAILED
  - [ ] 9.3.3 Test that invoice is NOT created when payment status changes to CANCELED
  - [ ] 9.3.4 Test that invoice creation doesn't break payment status update if invoice creation fails

## 10. Validation
- [x] 10.1 Run `openspec validate create-invoice-from-approved-payment --strict`
- [x] 10.2 Fix any validation errors
- [x] 10.3 Ensure all scenarios are covered by tests

