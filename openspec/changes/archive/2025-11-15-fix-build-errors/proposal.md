## Why

The build fails with "error: release version 17 not supported" because there's a mismatch between:
- The Java version configured in `pom.xml` (Java 17)
- The Java version mentioned in `project.md` (Java 21)
- The actual Java runtime environment (Java 11)

Spring Boot 3.5.7 requires Java 17 minimum, so we need to align the documentation and ensure the build configuration is consistent.

## What Changes

- **MODIFIED**: Update `project.md` to reflect Java 17 requirement (matching `pom.xml` and Spring Boot 3.5.7 requirements)
- **ADDED**: Document Java version requirement clearly in project documentation
- **ADDED**: Ensure build configuration consistently specifies Java 17

## Impact

- **Affected specs**: Build specification (documentation update)
- **Affected code**: 
  - `openspec/project.md` - Update Java version from 21 to 17
- **Fixes**: Build configuration inconsistency and documentation mismatch

