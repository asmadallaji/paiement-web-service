## Why

The system needs to prevent duplicate payment creation to avoid charging the same action twice. When a client attempts to create a payment for the same order and user that already has a PENDING payment, the system should return the existing payment instead of creating a duplicate.

## What Changes

- **MODIFIED**: Payment creation logic to check for existing PENDING payments before creating new ones
- **ADDED**: Idempotency check based on `orderId` + `userId` combination
- **ADDED**: Repository method to find existing PENDING payments by `orderId` and `userId`
- **ADDED**: Logging for duplicate payment detection cases
- **MODIFIED**: Payment creation service to return existing payment when duplicate detected

## Impact

- **Affected specs**: Payment creation capability (idempotency behavior)
- **Affected code**: 
  - `PaymentRepository` - Add query method to find existing payments
  - `PaymentService` - Add idempotency check logic
- **Behavior change**: Payment creation endpoint now returns existing payment instead of creating duplicate when conditions are met

