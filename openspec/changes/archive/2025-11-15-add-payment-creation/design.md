## Context

This change introduces the foundational payment creation capability for the Payment Service. The system follows an API-first approach using OpenAPI specifications, with code generation for DTOs and controller interfaces. The implementation uses Spring Boot 3 with JPA/Hibernate for persistence and H2 database for development.

## Goals / Non-Goals

### Goals
- Enable clients to create payments via REST API
- Enforce business rules through validation (amount > 0, required fields, allowed payment methods)
- Initialize payments with `PENDING` status and proper timestamps
- Persist payments to database with unique identifiers
- Follow OpenAPI-first development approach

### Non-Goals
- Payment status transitions (covered in future stories)
- Payment retrieval by ID (covered in future stories)
- Payment listing/filtering (covered in future stories)
- Idempotency handling (covered in Story A2)
- Integration with external payment gateways

## Decisions

### Decision: Payment ID Type
**What**: Use `Long` (auto-generated) for payment IDs
**Why**: 
- Simple and efficient for H2 database
- Standard JPA approach with `@GeneratedValue`
- Can be migrated to UUID later if needed for distributed systems
**Alternatives considered**: 
- UUID: Better for distributed systems but adds complexity for initial implementation
- String: More flexible but less efficient

### Decision: Payment Method Storage
**What**: Store payment method as `String` with validation against allowed values
**Why**: 
- Flexible for future payment methods
- Simple validation logic
- Can be converted to enum later if needed
**Alternatives considered**: 
- Enum: More type-safe but requires code changes for new methods

### Decision: Currency Storage
**What**: Store currency as `String` (ISO 4217 codes like "USD", "EUR")
**Why**: 
- Standard format (ISO 4217)
- Simple validation (non-empty)
- Can add enum validation later if needed
**Alternatives considered**: 
- Enum: Too restrictive for international use cases

### Decision: Timestamp Management
**What**: Use `LocalDateTime` with automatic setting in service layer
**Why**: 
- No timezone complexity for initial implementation
- Service layer ensures consistency
- Can add `@PrePersist`/`@PreUpdate` later if needed
**Alternatives considered**: 
- JPA lifecycle callbacks: More automatic but less explicit

### Decision: Validation Approach
**What**: Validate in service layer with custom exceptions
**Why**: 
- Business logic belongs in service layer
- Clear separation of concerns
- Easy to test
**Alternatives considered**: 
- Bean Validation annotations: Good for simple validation, but business rules (allowed payment methods) need service logic anyway

### Decision: OpenAPI Spec Location
**What**: Place OpenAPI spec in `src/main/resources/openapi/payment-api.yaml` (or similar standard location)
**Why**: 
- Standard location for OpenAPI specs
- Easy to reference in Maven plugin configuration
- Keeps API contract visible and version-controlled
**Alternatives considered**: 
- Separate `api/` directory: Also valid, but resources is more standard for Spring Boot

## Risks / Trade-offs

### Risk: OpenAPI Generator Configuration
**Mitigation**: Follow Spring Boot OpenAPI Generator plugin documentation, test generation early in implementation

### Risk: Validation Logic Duplication
**Mitigation**: Keep validation in service layer; OpenAPI spec validation is for contract documentation, service validation is for business rules

### Risk: Payment Method List Hardcoded
**Mitigation**: Start with hardcoded list (e.g., "CREDIT_CARD", "DEBIT_CARD", "PAYPAL"); can be externalized to configuration later

## Migration Plan

N/A - This is a new feature, not a migration.

## Open Questions

- What are the exact allowed payment methods? (Assumption: Will be defined as enum/constants in code)
- Should `orderId` be validated for format/pattern? (Assumption: No format validation for now, just optional)
- Should we log payment creation events? (Assumption: Yes, but basic logging for now)

