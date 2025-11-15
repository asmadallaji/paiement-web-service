## Why

When creating a payment with an `orderId` field, Jackson fails to deserialize the JSON value into a `JsonNullable` object, causing a 500 Internal Server Error. The error message indicates: "Cannot construct instance of `org.openapitools.jackson.nullable.JsonNullable` (no Creators, like default constructor, exist): no String-argument constructor/factory method to deserialize from String value".

This happens because Jackson needs the `JsonNullableModule` to be registered to properly handle `JsonNullable` deserialization, but it's not automatically configured in Spring Boot.

## What Changes

- **ADDED**: Jackson configuration class to register `JsonNullableModule` for proper `JsonNullable` deserialization
- **ADDED**: Configuration to enable Spring Boot to automatically handle `JsonNullable` in request/response bodies

## Impact

- **Affected specs**: Payment creation capability (bug fix)
- **Affected code**: 
  - New `JacksonConfig` class in `com.asma.paymentservice.config` package
- **Fixes**: 500 error when creating payments with optional fields like `orderId` that use `JsonNullable`

