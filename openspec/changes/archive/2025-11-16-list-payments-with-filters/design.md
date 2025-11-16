## Context

This change adds payment listing functionality with filtering and pagination capabilities. Administrators need to view and filter payments by various criteria to monitor payment activity effectively.

## Goals / Non-Goals

### Goals
- Provide endpoint to list all payments
- Support filtering by status, userId, and orderId
- Implement pagination to handle large result sets efficiently
- Return paginated response with metadata (total count, page info)
- Maintain backward compatibility (no breaking changes)

### Non-Goals
- Sorting/ordering (can be added in future iteration)
- Date range filtering (can be added in future iteration)
- Complex search queries (simple exact match filters only)
- Export functionality (out of scope)

## Decisions

### Decision: Pagination Strategy
**What**: Use page-based pagination with `page` (0-indexed) and `size` parameters
**Why**: 
- Standard REST API pattern, easy to understand and implement
- Works well with Spring Data JPA's `Pageable` interface
- Provides predictable behavior for clients
**Alternatives considered**: 
- Cursor-based pagination: More complex, better for very large datasets but not needed initially
- Offset/limit: Similar to page-based but less standard in Spring ecosystem

### Decision: Filter Parameters
**What**: Use query parameters `status`, `userId`, `orderId` (all optional)
**Why**: 
- Simple and intuitive REST API design
- Matches business requirements from backlog
- Easy to extend with additional filters later
**Alternatives considered**: 
- Single filter parameter with JSON: More flexible but complex to parse
- POST with filter body: Not RESTful for read operations

### Decision: Response Format
**What**: Return paginated response with `content` (array of payments), `totalElements`, `totalPages`, `page`, `size`
**Why**: 
- Standard pagination metadata format
- Compatible with Spring Data's `Page` interface
- Provides all necessary information for client pagination controls
**Alternatives considered**: 
- Simple array: Loses pagination metadata
- Custom format: Less standard, more work

### Decision: Default Pagination
**What**: Default page size of 20, maximum page size of 100
**Why**: 
- Reasonable default prevents excessive data transfer
- Maximum prevents abuse and performance issues
- Common industry practice
**Alternatives considered**: 
- No default: Requires clients to always specify, less user-friendly
- Larger defaults: May cause performance issues with large datasets

## Risks / Trade-offs

### Risk: Performance with Large Datasets
**Mitigation**: Use pagination to limit result size, add database indexes on filter columns if needed in future

### Risk: Complex Query Combinations
**Mitigation**: Start with simple exact match filters, can enhance with more complex queries later if needed

### Trade-off: Simplicity vs. Flexibility
**Decision**: Start with simple exact match filters. Can add range queries, partial matches, etc. in future iterations based on actual needs.

## Migration Plan

N/A - This is a new feature, not a migration. Existing endpoints are not affected.

## Open Questions

- Should we add sorting by default (e.g., by createdAt descending)? (Assumption: Yes, sort by createdAt descending for most recent first)
- Should we limit the maximum page size? (Assumption: Yes, max 100 items per page)
- Should filters be case-sensitive? (Assumption: Yes for exact matches, can be enhanced later)

