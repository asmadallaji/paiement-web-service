## 1. OpenAPI Specification
- [x] 1.1 Add GET `/payments/{id}` endpoint to `payment-api.yaml`
- [x] 1.2 Define path parameter `id` (integer, required)
- [x] 1.3 Define response schemas: 200 (PaymentResponse), 404 (ErrorResponse)
- [ ] 1.4 Regenerate DTOs and API interfaces using OpenAPI Generator (requires mvn generate-sources)

## 2. Service Layer
- [x] 2.1 Add `getPaymentById(Long id)` method to `PaymentService`
- [x] 2.2 Implement logic to fetch payment from repository
- [x] 2.3 Handle case where payment is not found
- [x] 2.4 Map Payment entity to PaymentResponse DTO
- [x] 2.5 Add logging for payment retrieval

## 3. Controller Layer
- [x] 3.1 Implement `getPaymentById` method in `PaymentController`
- [x] 3.2 Handle `PaymentNotFoundException` and return 404 status
- [x] 3.3 Return 200 OK with PaymentResponse when payment is found

## 4. Exception Handling
- [x] 4.1 Create `PaymentNotFoundException` custom exception
- [x] 4.2 Add exception handler in `GlobalExceptionHandler` for 404 responses
- [x] 4.3 Map exception to ErrorResponse DTO

## 5. Testing
- [x] 5.1 Unit test: `getPaymentById` returns payment when found
- [x] 5.2 Unit test: `getPaymentById` throws exception when not found
- [x] 5.3 Integration test: GET `/payments/{id}` returns 200 with payment data
- [x] 5.4 Integration test: GET `/payments/{id}` returns 404 when payment not found
- [x] 5.5 Integration test: GET `/payments/{id}` returns all payment fields correctly

## 6. Validation
- [x] 6.1 Run `openspec validate get-payment-by-id --strict` and resolve any issues
- [x] 6.2 Verify payment retrieval works correctly in integration tests
- [ ] 6.3 Verify OpenAPI spec is valid and generates correct code (requires mvn generate-sources)

