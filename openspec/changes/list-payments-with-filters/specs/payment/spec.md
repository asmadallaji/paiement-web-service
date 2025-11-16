## ADDED Requirements

### Requirement: Payment Listing with Filters and Pagination
The system SHALL provide an API endpoint to list payments with optional filtering by status, userId, and orderId, and support pagination. The endpoint SHALL return a paginated list of payments with metadata including total count, total pages, current page, and page size. All filter parameters are optional and can be combined. Pagination parameters have default values and maximum limits.

#### Scenario: List all payments without filters
- **WHEN** a client sends a GET request to `/payments` without any query parameters
- **THEN** the system SHALL return a paginated list of all payments with status 200 OK
- **AND** the response SHALL include pagination metadata (totalElements, totalPages, page, size)
- **AND** the default page size SHALL be 20
- **AND** the default page SHALL be 0 (first page)
- **AND** payments SHALL be sorted by createdAt in descending order (most recent first)

#### Scenario: List payments filtered by status
- **WHEN** a client sends a GET request to `/payments?status=PENDING`
- **THEN** the system SHALL return only payments with status PENDING
- **AND** the response SHALL include pagination metadata
- **AND** the totalElements SHALL reflect the count of filtered payments

#### Scenario: List payments filtered by userId
- **WHEN** a client sends a GET request to `/payments?userId=user123`
- **THEN** the system SHALL return only payments for the specified userId
- **AND** the response SHALL include pagination metadata
- **AND** the totalElements SHALL reflect the count of filtered payments

#### Scenario: List payments filtered by orderId
- **WHEN** a client sends a GET request to `/payments?orderId=order456`
- **THEN** the system SHALL return only payments with the specified orderId
- **AND** the response SHALL include pagination metadata
- **AND** the totalElements SHALL reflect the count of filtered payments

#### Scenario: List payments with multiple filters
- **WHEN** a client sends a GET request to `/payments?status=PENDING&userId=user123`
- **THEN** the system SHALL return only payments matching all specified filters
- **AND** the response SHALL include pagination metadata
- **AND** the totalElements SHALL reflect the count of payments matching all filters

#### Scenario: List payments with pagination
- **WHEN** a client sends a GET request to `/payments?page=0&size=10`
- **THEN** the system SHALL return the first 10 payments (page 0)
- **AND** the response SHALL include pagination metadata with page=0, size=10
- **AND** the totalPages SHALL be calculated based on totalElements and size

#### Scenario: Navigate to next page
- **WHEN** a client sends a GET request to `/payments?page=1&size=10`
- **THEN** the system SHALL return payments 11-20 (second page)
- **AND** the response SHALL include pagination metadata with page=1, size=10

#### Scenario: Invalid pagination parameters
- **WHEN** a client sends a GET request to `/payments?page=-1`
- **OR** a client sends a GET request to `/payments?size=0`
- **OR** a client sends a GET request to `/payments?size=200`
- **THEN** the system SHALL return status 400 Bad Request
- **AND** the response SHALL include an ErrorResponse with code 400 and validation error details
- **AND** page SHALL be >= 0
- **AND** size SHALL be between 1 and 100 (inclusive)

#### Scenario: Empty result set
- **WHEN** a client sends a GET request to `/payments?status=APPROVED`
- **AND** no payments exist with status APPROVED
- **THEN** the system SHALL return status 200 OK
- **AND** the response SHALL include an empty content array
- **AND** totalElements SHALL be 0
- **AND** totalPages SHALL be 0

