## ADDED Requirements

### Requirement: Invoice Listing with Filters and Pagination
The system SHALL provide an API endpoint to list invoices with optional filtering by userId, status, and issue date period, and support pagination. The endpoint SHALL return a paginated list of invoices with metadata including total count, total pages, current page, and page size. All filter parameters are optional and can be combined. Pagination parameters have default values and maximum limits. Results SHALL be sorted by issueDate in descending order (most recent first).

#### Scenario: List all invoices without filters
- **WHEN** a client sends a GET request to `/invoices` without any query parameters
- **THEN** the system SHALL return a paginated list of all invoices with status 200 OK
- **AND** the response SHALL include pagination metadata (totalElements, totalPages, page, size)
- **AND** the default page size SHALL be 20
- **AND** the default page SHALL be 0 (first page)
- **AND** invoices SHALL be sorted by issueDate in descending order (most recent first)

#### Scenario: List invoices filtered by status
- **WHEN** a client sends a GET request to `/invoices?status=CREATED`
- **THEN** the system SHALL return only invoices with status CREATED
- **AND** the response SHALL include pagination metadata
- **AND** the totalElements SHALL reflect the count of filtered invoices

#### Scenario: List invoices filtered by userId
- **WHEN** a client sends a GET request to `/invoices?userId=user123`
- **THEN** the system SHALL return only invoices for the specified userId
- **AND** the response SHALL include pagination metadata
- **AND** the totalElements SHALL reflect the count of filtered invoices

#### Scenario: List invoices filtered by date period
- **WHEN** a client sends a GET request to `/invoices?fromDate=2024-01-01&toDate=2024-01-31`
- **THEN** the system SHALL return only invoices with issueDate between fromDate and toDate (inclusive)
- **AND** the response SHALL include pagination metadata
- **AND** the totalElements SHALL reflect the count of filtered invoices

#### Scenario: List invoices with multiple filters
- **WHEN** a client sends a GET request to `/invoices?status=CREATED&userId=user123`
- **THEN** the system SHALL return only invoices matching all specified filters
- **AND** the response SHALL include pagination metadata
- **AND** the totalElements SHALL reflect the count of invoices matching all filters

#### Scenario: List invoices with pagination
- **WHEN** a client sends a GET request to `/invoices?page=0&size=10`
- **THEN** the system SHALL return the first 10 invoices (page 0)
- **AND** the response SHALL include pagination metadata with page=0, size=10
- **AND** the totalPages SHALL be calculated based on totalElements and size

#### Scenario: Navigate to next page
- **WHEN** a client sends a GET request to `/invoices?page=1&size=10`
- **THEN** the system SHALL return invoices 11-20 (second page)
- **AND** the response SHALL include pagination metadata with page=1, size=10

#### Scenario: Invalid pagination parameters
- **WHEN** a client sends a GET request to `/invoices?page=-1`
- **OR** a client sends a GET request to `/invoices?size=0`
- **OR** a client sends a GET request to `/invoices?size=200`
- **THEN** the system SHALL return status 400 Bad Request
- **AND** the response SHALL include an ErrorResponse with code 400 and validation error details
- **AND** page SHALL be >= 0
- **AND** size SHALL be between 1 and 100 (inclusive)

#### Scenario: Invalid date range
- **WHEN** a client sends a GET request to `/invoices?fromDate=2024-01-31&toDate=2024-01-01`
- **AND** fromDate is after toDate
- **THEN** the system SHALL return status 400 Bad Request
- **AND** the response SHALL include an ErrorResponse with code 400 and validation error details indicating invalid date range

#### Scenario: Empty result set
- **WHEN** a client sends a GET request to `/invoices?status=PAID`
- **AND** no invoices exist with status PAID
- **THEN** the system SHALL return status 200 OK
- **AND** the response SHALL include an empty content array
- **AND** totalElements SHALL be 0
- **AND** totalPages SHALL be 0

