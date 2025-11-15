## 1. Jackson Configuration
- [x] 1.1 Create `JacksonConfig` configuration class
- [x] 1.2 Register `JsonNullableModule` with ObjectMapper
- [x] 1.3 Ensure configuration is automatically picked up by Spring Boot

## 2. Testing
- [x] 2.1 Test payment creation with `orderId` field (added test with direct JSON deserialization)
- [x] 2.2 Test payment creation without `orderId` field (existing test covers this)
- [x] 2.3 Verify no 500 errors occur during deserialization (added test with null orderId)

## 3. Validation
- [x] 3.1 Run `openspec validate fix-jsonnullable-deserialization --strict` and resolve any issues
- [x] 3.2 Verify payment creation endpoint works correctly with optional fields (tests added to verify JSON deserialization)

