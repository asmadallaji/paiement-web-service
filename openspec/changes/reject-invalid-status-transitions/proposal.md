## Why

The system needs to enforce strict payment status transition rules to maintain data integrity and prevent invalid state changes. Currently, there is no centralized validation logic to reject illegal transitions (e.g., attempting to change an APPROVED payment back to PENDING, or using an unknown status). This proposal ensures that all status transitions follow business rules and provides clear error responses when violations occur.

## What Changes

- **ADDED**: Centralized status transition validation logic in the business layer
- **ADDED**: Exception handling for invalid status transitions returning HTTP 409 Conflict
- **ADDED**: Validation for unknown/invalid target status values
- **ADDED**: Business error logging for invalid transition attempts
- **ADDED**: Clear error messages indicating the current status, target status, and why the transition is invalid

## Impact

- **Affected specs**: Payment capability (adds status transition validation requirement)
- **Affected code**: 
  - `PaymentService` - Add centralized transition validation method
  - Exception handling - Add `InvalidStatusTransitionException` for 409 responses
  - `GlobalExceptionHandler` - Map `InvalidStatusTransitionException` to 409 Conflict response
- **Dependencies**: This change assumes the status update endpoint exists (from `update-payment-status` change)
- **Behavior change**: Invalid status transitions will now be explicitly rejected with clear error messages instead of potentially being silently ignored or causing unexpected behavior

