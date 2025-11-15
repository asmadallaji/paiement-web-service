## 1. Repository Layer
- [x] 1.1 Add query method to `PaymentRepository` to find existing PENDING payments by `orderId` and `userId`
- [x] 1.2 Handle case where `orderId` is null (idempotency only applies when orderId is provided)

## 2. Service Layer
- [x] 2.1 Modify `createPayment` method to check for existing PENDING payments before creating
- [x] 2.2 Implement idempotency logic: if `orderId` is provided, check for existing PENDING payment with same `orderId` + `userId`
- [x] 2.3 Return existing payment if duplicate found instead of creating new one
- [x] 2.4 Add logging when duplicate payment is detected and existing payment is returned

## 3. Testing
- [x] 3.1 Test payment creation with same `orderId` + `userId` returns existing payment
- [x] 3.2 Test payment creation without `orderId` still creates new payment (no idempotency check)
- [x] 3.3 Test payment creation with different `orderId` creates new payment
- [x] 3.4 Test payment creation with same `orderId` but different `userId` creates new payment
- [x] 3.5 Test payment creation with same `orderId` + `userId` but different status (e.g., APPROVED) creates new payment
- [x] 3.6 Verify logging occurs when duplicate is detected

## 4. Validation
- [x] 4.1 Run `openspec validate add-payment-idempotency --strict` and resolve any issues
- [x] 4.2 Verify idempotency behavior works correctly in integration tests

