## 1. Repository Layer
- [x] 1.1 Add method `Page<Invoice> findByStatusOrderByIssueDateDesc(InvoiceStatus status, Pageable pageable)` to InvoiceRepository
- [x] 1.2 Add method `Page<Invoice> findByUserIdOrderByIssueDateDesc(String userId, Pageable pageable)` to InvoiceRepository
- [x] 1.3 Add method `Page<Invoice> findByStatusAndUserIdOrderByIssueDateDesc(InvoiceStatus status, String userId, Pageable pageable)` to InvoiceRepository
- [x] 1.4 Add method `Page<Invoice> findByIssueDateBetweenOrderByIssueDateDesc(LocalDate fromDate, LocalDate toDate, Pageable pageable)` to InvoiceRepository
- [x] 1.5 Add method `Page<Invoice> findByStatusAndIssueDateBetweenOrderByIssueDateDesc(InvoiceStatus status, LocalDate fromDate, LocalDate toDate, Pageable pageable)` to InvoiceRepository
- [x] 1.6 Add method `Page<Invoice> findByUserIdAndIssueDateBetweenOrderByIssueDateDesc(String userId, LocalDate fromDate, LocalDate toDate, Pageable pageable)` to InvoiceRepository
- [x] 1.7 Add method `Page<Invoice> findByStatusAndUserIdAndIssueDateBetweenOrderByIssueDateDesc(InvoiceStatus status, String userId, LocalDate fromDate, LocalDate toDate, Pageable pageable)` to InvoiceRepository
- [x] 1.8 Add method `@Query("SELECT i FROM Invoice i ORDER BY i.issueDate DESC") Page<Invoice> findAllOrderByIssueDateDesc(Pageable pageable)` to InvoiceRepository

## 2. Service Layer
- [x] 2.1 Add `listInvoices` method to InvoiceService with parameters: status, userId, fromDate, toDate, page, size
- [x] 2.2 Implement pagination parameter validation (page >= 0, size between 1 and 100)
- [x] 2.3 Implement date range validation (fromDate <= toDate)
- [x] 2.4 Implement status string to enum conversion with error handling
- [x] 2.5 Implement filter selection logic to choose appropriate repository method based on provided filters
- [x] 2.6 Implement mapping from Invoice entity to InvoiceResponse DTO
- [x] 2.7 Implement mapping from Page<Invoice> to InvoiceListResponse
- [x] 2.8 Add logging for invoice listing operations

## 3. OpenAPI Specification
- [x] 3.1 Update OpenAPI spec GET /invoices endpoint to support both listing and single invoice retrieval:
  - [x] 3.1.1 Make paymentId parameter optional (currently required)
  - [x] 3.1.2 When paymentId is provided, return single InvoiceResponse (existing behavior)
  - [x] 3.1.3 When paymentId is not provided, return InvoiceListResponse with paginated results
- [x] 3.2 Add query parameters for listing: status (optional enum), userId (optional string), fromDate (optional date), toDate (optional date), page (optional integer, default 0), size (optional integer, default 20, max 100)
- [x] 3.3 Define InvoiceListResponse schema with: content (array of InvoiceResponse), totalElements, totalPages, page, size
- [x] 3.4 Update endpoint responses to support both InvoiceResponse (when paymentId provided) and InvoiceListResponse (when listing)
- [x] 3.5 Define error responses (400 for invalid parameters, 500 for server errors)

## 4. Code Generation
- [x] 4.1 Run OpenAPI Generator to generate InvoiceListResponse DTO and updated InvoicesApi interface
- [x] 4.2 Verify generated code is in correct package structure

## 5. Controller Implementation
- [x] 5.1 Update InvoiceController to implement listInvoices method from generated InvoicesApi interface
- [x] 5.2 Handle all filter combinations (status, userId, date range, pagination)
- [x] 5.3 Add proper exception handling and error responses
- [x] 5.4 Ensure GET /invoices endpoint works for listing (not just paymentId query)

## 6. Testing
- [x] 6.1 Write unit tests for InvoiceService.listInvoices:
  - [x] 6.1.1 Test listing all invoices without filters
  - [x] 6.1.2 Test filtering by status
  - [x] 6.1.3 Test filtering by userId
  - [x] 6.1.4 Test filtering by date range
  - [x] 6.1.5 Test multiple filters combination
  - [x] 6.1.6 Test pagination
  - [x] 6.1.7 Test invalid pagination parameters
  - [x] 6.1.8 Test invalid date range (fromDate > toDate)
  - [x] 6.1.9 Test empty result set
- [x] 6.2 Write integration tests for InvoiceController:
  - [x] 6.2.1 Test GET /invoices endpoint without filters
  - [x] 6.2.2 Test GET /invoices with status filter
  - [x] 6.2.3 Test GET /invoices with userId filter
  - [x] 6.2.4 Test GET /invoices with date range filter
  - [x] 6.2.5 Test GET /invoices with multiple filters
  - [x] 6.2.6 Test GET /invoices with pagination
  - [x] 6.2.7 Test error cases (400 for invalid parameters)

## 7. Validation
- [x] 7.1 Run `openspec validate list-invoices-with-filters --strict`
- [x] 7.2 Fix any validation errors
- [x] 7.3 Ensure all scenarios are covered by tests

