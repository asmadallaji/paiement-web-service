## Context

This change adds idempotency to payment creation to prevent duplicate payments for the same order and user. The requirement is to check if a PENDING payment already exists for the same `orderId` + `userId` combination before creating a new one.

## Goals / Non-Goals

### Goals
- Prevent duplicate payment creation for the same order and user
- Return existing PENDING payment when duplicate creation is attempted
- Log duplicate detection cases for monitoring and debugging
- Maintain backward compatibility (idempotency only applies when `orderId` is provided)

### Non-Goals
- Idempotency for payments without `orderId` (only applies when orderId is present)
- Idempotency across different payment statuses (only checks PENDING status)
- Idempotency key management system (simple orderId + userId check)

## Decisions

### Decision: Idempotency Key
**What**: Use `orderId` + `userId` combination as idempotency key
**Why**: 
- Simple and effective for preventing duplicate payments for the same order
- Matches business requirement: "ne pas facturer deux fois la même action"
- Only applies when `orderId` is provided (optional field)
**Alternatives considered**: 
- UUID-based idempotency key: More complex, requires additional field in request
- Only `orderId`: Less precise, doesn't account for multiple users

### Decision: Status Check
**What**: Only check for PENDING payments when detecting duplicates
**Why**: 
- Once a payment is APPROVED, FAILED, or CANCELED, it's no longer active
- A new payment can be created if the previous one is in a final state
- Matches requirement: "encore en PENDING"
**Alternatives considered**: 
- Check all statuses: Too restrictive, prevents legitimate retries
- Check only APPROVED: Doesn't prevent duplicates for failed payments

### Decision: Behavior When Duplicate Found
**What**: Return existing payment with 201 Created status (same as new payment creation)
**Why**: 
- Idempotent operation should return same result on retry
- Client receives the payment they intended to create
- No breaking change to API contract
**Alternatives considered**: 
- Return 200 OK: Less clear that it's a creation endpoint
- Return 409 Conflict: Indicates error, but this is expected idempotent behavior

### Decision: Null orderId Handling
**What**: Skip idempotency check when `orderId` is null
**Why**: 
- Idempotency key requires both `orderId` and `userId`
- Payments without `orderId` may be legitimate duplicates (e.g., different payment attempts)
- Matches requirement: idempotency based on `orderId` + `userId`
**Alternatives considered**: 
- Use only `userId`: Too restrictive, prevents multiple payments from same user
- Require `orderId`: Breaking change, makes optional field required

## Risks / Trade-offs

### Risk: Race Condition
**Mitigation**: Use `@Transactional` annotation to ensure atomic check-and-create operation. Consider database-level unique constraint if needed in future.

### Risk: Performance Impact
**Mitigation**: Add database index on `(orderId, userId, status)` if query performance becomes an issue. Initial implementation is simple query.

### Trade-off: Simplicity vs. Completeness
**Decision**: Start with simple `orderId` + `userId` + `PENDING` check. Can be enhanced later with more sophisticated idempotency key management if needed.

## Migration Plan

N/A - This is a new feature, not a migration. Existing payments are not affected.

## Open Questions

- Should we add a database index on `(orderId, userId, status)` for performance? (Assumption: Start without, add if needed)
- Should we log the duplicate detection at INFO or WARN level? (Assumption: INFO level for normal idempotent behavior)

