## Why

The OpenAPI Generator generates DTOs that use `org.openapitools.jackson.nullable.JsonNullable`, but the required dependency `jackson-databind-nullable` is missing from `pom.xml`, causing compilation errors.

## What Changes

- **ADDED**: Missing dependency `org.openapitools.jackson.databind-nullable` to `pom.xml`
- This dependency is required for the generated OpenAPI DTOs that use `JsonNullable` for optional fields

## Impact

- **Affected specs**: None (bug fix, no spec changes)
- **Affected code**: 
  - `pom.xml` - Add missing dependency
- **Fixes**: Compilation error in generated DTOs (`ErrorResponse.java`, `CreatePaymentRequest.java`, `PaymentResponse.java`)

