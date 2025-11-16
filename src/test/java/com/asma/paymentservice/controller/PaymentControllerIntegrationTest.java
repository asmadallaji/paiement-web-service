package com.asma.paymentservice.controller;

import com.asma.paymentservice.dto.CreatePaymentRequest;
import com.asma.paymentservice.dto.UpdatePaymentStatusRequest;
import com.asma.paymentservice.repository.PaymentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openapitools.jackson.nullable.JsonNullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.asma.paymentservice.entity.Payment;
import com.asma.paymentservice.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PaymentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PaymentRepository paymentRepository;

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();
    }

    @Test
    void createPayment_WithValidRequest_ShouldReturn201() throws Exception {
        // Given
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setAmount(99.99);
        request.setCurrency("USD");
        request.setMethod(CreatePaymentRequest.MethodEnum.CREDIT_CARD);
        request.setUserId("user123");
        request.setOrderId(JsonNullable.of("order456"));

        // When/Then
        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.amount").value(99.99))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.method").value("CREDIT_CARD"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.userId").value("user123"))
                .andExpect(jsonPath("$.orderId").value("order456"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void createPayment_WithValidRequestWithoutOrderId_ShouldReturn201() throws Exception {
        // Given
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setAmount(50.00);
        request.setCurrency("EUR");
        request.setMethod(CreatePaymentRequest.MethodEnum.PAYPAL);
        request.setUserId("user456");

        // When/Then
        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").doesNotExist());
    }

    @Test
    void createPayment_WithNegativeAmount_ShouldReturn400() throws Exception {
        // Given
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setAmount(-10.00);
        request.setCurrency("USD");
        request.setMethod(CreatePaymentRequest.MethodEnum.CREDIT_CARD);
        request.setUserId("user123");

        // When/Then
        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details").value("Amount must be greater than 0"));
    }

    @Test
    void createPayment_WithEmptyCurrency_ShouldReturn400() throws Exception {
        // Given
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setAmount(99.99);
        request.setCurrency("");
        request.setMethod(CreatePaymentRequest.MethodEnum.CREDIT_CARD);
        request.setUserId("user123");

        // When/Then
        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.details").value("Currency must not be empty"));
    }

    @Test
    void createPayment_WithInvalidMethod_ShouldReturn400() throws Exception {
        // Given
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setAmount(99.99);
        request.setCurrency("USD");
        request.setMethod(null);
        request.setUserId("user123");

        // When/Then
        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.details").value("Payment method is required"));
    }

    @Test
    void createPayment_WithEmptyUserId_ShouldReturn400() throws Exception {
        // Given
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setAmount(99.99);
        request.setCurrency("USD");
        request.setMethod(CreatePaymentRequest.MethodEnum.CREDIT_CARD);
        request.setUserId("");

        // When/Then
        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.details").value("UserId must not be empty"));
    }

    @Test
    void createPayment_WithOrderIdInJson_ShouldDeserializeCorrectly() throws Exception {
        // Given - Test direct JSON deserialization (not using ObjectMapper serialization)
        String jsonRequest = "{\n" +
                "    \"amount\": 75.50,\n" +
                "    \"currency\": \"USD\",\n" +
                "    \"method\": \"CREDIT_CARD\",\n" +
                "    \"userId\": \"user999\",\n" +
                "    \"orderId\": \"order789\"\n" +
                "}";

        // When/Then
        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.amount").value(75.50))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.method").value("CREDIT_CARD"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.userId").value("user999"))
                .andExpect(jsonPath("$.orderId").value("order789"));
    }

    @Test
    void createPayment_WithNullOrderIdInJson_ShouldDeserializeCorrectly() throws Exception {
        // Given - Test JSON with null orderId
        String jsonRequest = "{\n" +
                "    \"amount\": 100.00,\n" +
                "    \"currency\": \"EUR\",\n" +
                "    \"method\": \"PAYPAL\",\n" +
                "    \"userId\": \"user888\",\n" +
                "    \"orderId\": null\n" +
                "}";

        // When/Then
        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.orderId").isEmpty());
    }

    @Test
    void createPayment_WithDuplicateOrderIdAndUserId_ShouldReturnExistingPayment() throws Exception {
        // Given - Create first payment
        CreatePaymentRequest firstRequest = new CreatePaymentRequest();
        firstRequest.setAmount(99.99);
        firstRequest.setCurrency("USD");
        firstRequest.setMethod(CreatePaymentRequest.MethodEnum.CREDIT_CARD);
        firstRequest.setUserId("user123");
        firstRequest.setOrderId(org.openapitools.jackson.nullable.JsonNullable.of("order456"));

        String firstResponse = mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long firstPaymentId = objectMapper.readTree(firstResponse).get("id").asLong();

        // When - Attempt to create duplicate payment with same orderId and userId
        CreatePaymentRequest duplicateRequest = new CreatePaymentRequest();
        duplicateRequest.setAmount(150.00); // Different amount
        duplicateRequest.setCurrency("EUR"); // Different currency
        duplicateRequest.setMethod(CreatePaymentRequest.MethodEnum.PAYPAL); // Different method
        duplicateRequest.setUserId("user123"); // Same userId
        duplicateRequest.setOrderId(org.openapitools.jackson.nullable.JsonNullable.of("order456")); // Same orderId

        // Then - Should return existing payment (idempotency)
        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(firstPaymentId)) // Same ID as first payment
                .andExpect(jsonPath("$.amount").value(99.99)) // Original amount
                .andExpect(jsonPath("$.currency").value("USD")) // Original currency
                .andExpect(jsonPath("$.method").value("CREDIT_CARD")) // Original method
                .andExpect(jsonPath("$.orderId").value("order456"))
                .andExpect(jsonPath("$.userId").value("user123"));

        // Verify only one payment exists in database
        assertEquals(1, paymentRepository.count());
    }

    @Test
    void createPayment_WithoutOrderId_ShouldCreateNewPaymentEvenIfDuplicate() throws Exception {
        // Given - Create first payment without orderId
        CreatePaymentRequest firstRequest = new CreatePaymentRequest();
        firstRequest.setAmount(50.00);
        firstRequest.setCurrency("USD");
        firstRequest.setMethod(CreatePaymentRequest.MethodEnum.CREDIT_CARD);
        firstRequest.setUserId("user123");

        String firstResponse = mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long firstPaymentId = objectMapper.readTree(firstResponse).get("id").asLong();

        // When - Attempt to create another payment without orderId (same userId)
        CreatePaymentRequest secondRequest = new CreatePaymentRequest();
        secondRequest.setAmount(75.00);
        secondRequest.setCurrency("EUR");
        secondRequest.setMethod(CreatePaymentRequest.MethodEnum.PAYPAL);
        secondRequest.setUserId("user123"); // Same userId but no orderId

        // Then - Should create new payment (no idempotency check without orderId)
        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(org.hamcrest.Matchers.not(firstPaymentId))) // Different ID
                .andExpect(jsonPath("$.amount").value(75.00)) // New amount
                .andExpect(jsonPath("$.currency").value("EUR")) // New currency
                .andExpect(jsonPath("$.method").value("PAYPAL")); // New method

        // Verify two payments exist in database
        assertEquals(2, paymentRepository.count());
    }

    @Test
    void createPayment_WithDifferentOrderId_ShouldCreateNewPayment() throws Exception {
        // Given - Create first payment
        CreatePaymentRequest firstRequest = new CreatePaymentRequest();
        firstRequest.setAmount(99.99);
        firstRequest.setCurrency("USD");
        firstRequest.setMethod(CreatePaymentRequest.MethodEnum.CREDIT_CARD);
        firstRequest.setUserId("user123");
        firstRequest.setOrderId(org.openapitools.jackson.nullable.JsonNullable.of("order456"));

        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isCreated());

        // When - Create payment with different orderId
        CreatePaymentRequest secondRequest = new CreatePaymentRequest();
        secondRequest.setAmount(150.00);
        secondRequest.setCurrency("EUR");
        secondRequest.setMethod(CreatePaymentRequest.MethodEnum.PAYPAL);
        secondRequest.setUserId("user123"); // Same userId
        secondRequest.setOrderId(org.openapitools.jackson.nullable.JsonNullable.of("order999")); // Different orderId

        // Then - Should create new payment
        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.orderId").value("order999"))
                .andExpect(jsonPath("$.amount").value(150.00));

        // Verify two payments exist in database
        assertEquals(2, paymentRepository.count());
    }

    @Test
    void createPayment_WithSameOrderIdButDifferentUserId_ShouldCreateNewPayment() throws Exception {
        // Given - Create first payment
        CreatePaymentRequest firstRequest = new CreatePaymentRequest();
        firstRequest.setAmount(99.99);
        firstRequest.setCurrency("USD");
        firstRequest.setMethod(CreatePaymentRequest.MethodEnum.CREDIT_CARD);
        firstRequest.setUserId("user123");
        firstRequest.setOrderId(org.openapitools.jackson.nullable.JsonNullable.of("order456"));

        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isCreated());

        // When - Create payment with same orderId but different userId
        CreatePaymentRequest secondRequest = new CreatePaymentRequest();
        secondRequest.setAmount(200.00);
        secondRequest.setCurrency("GBP");
        secondRequest.setMethod(CreatePaymentRequest.MethodEnum.BANK_TRANSFER);
        secondRequest.setUserId("user999"); // Different userId
        secondRequest.setOrderId(org.openapitools.jackson.nullable.JsonNullable.of("order456")); // Same orderId

        // Then - Should create new payment
        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.orderId").value("order456"))
                .andExpect(jsonPath("$.userId").value("user999"))
                .andExpect(jsonPath("$.amount").value(200.00));

        // Verify two payments exist in database
        assertEquals(2, paymentRepository.count());
    }

    @Test
    void getPaymentById_WithExistingId_ShouldReturn200() throws Exception {
        // Given - Create a payment first
        CreatePaymentRequest createRequest = new CreatePaymentRequest();
        createRequest.setAmount(99.99);
        createRequest.setCurrency("USD");
        createRequest.setMethod(CreatePaymentRequest.MethodEnum.CREDIT_CARD);
        createRequest.setUserId("user123");
        createRequest.setOrderId(org.openapitools.jackson.nullable.JsonNullable.of("order456"));

        String createResponse = mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long paymentId = objectMapper.readTree(createResponse).get("id").asLong();

        // When/Then - Retrieve the payment
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/payments/" + paymentId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(paymentId))
                .andExpect(jsonPath("$.amount").value(99.99))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.method").value("CREDIT_CARD"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.userId").value("user123"))
                .andExpect(jsonPath("$.orderId").value("order456"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void getPaymentById_WithNonExistentId_ShouldReturn404() throws Exception {
        // Given - Non-existent payment ID
        Long nonExistentId = 999L;

        // When/Then
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/payments/" + nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("Payment not found"))
                .andExpect(jsonPath("$.details").value("Payment not found with ID: " + nonExistentId));
    }

    @Test
    void getPaymentById_WithInvalidIdFormat_ShouldReturn400() throws Exception {
        // Given - Invalid ID format (non-numeric)
        String invalidId = "abc";

        // When/Then
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/payments/" + invalidId))
                .andExpect(status().isBadRequest());
    }

    // Status Update Integration Tests

    @Test
    void updatePaymentStatus_WithValidTransition_ShouldReturn200() throws Exception {
        // Given - Create a payment with PENDING status
        Payment payment = Payment.builder()
                .amount(BigDecimal.valueOf(99.99))
                .currency("USD")
                .method("CREDIT_CARD")
                .status(PaymentStatus.PENDING)
                .userId("user123")
                .orderId("order456")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        Payment savedPayment = paymentRepository.save(payment);
        Long paymentId = savedPayment.getId();

        // When - Update status from PENDING to APPROVED
        UpdatePaymentStatusRequest requestBody = new UpdatePaymentStatusRequest();
        requestBody.setStatus(UpdatePaymentStatusRequest.StatusEnum.APPROVED);

        // Then
        mockMvc.perform(patch("/payments/" + paymentId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(paymentId))
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void updatePaymentStatus_WithInvalidTransitionFromTerminalState_ShouldReturn409() throws Exception {
        // Given - Create a payment with APPROVED status (terminal state)
        Payment payment = Payment.builder()
                .amount(BigDecimal.valueOf(99.99))
                .currency("USD")
                .method("CREDIT_CARD")
                .status(PaymentStatus.APPROVED)
                .userId("user123")
                .orderId("order456")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        Payment savedPayment = paymentRepository.save(payment);
        Long paymentId = savedPayment.getId();

        // When - Attempt to update status from APPROVED to FAILED (invalid transition - terminal state cannot change)
        UpdatePaymentStatusRequest requestBody = new UpdatePaymentStatusRequest();
        requestBody.setStatus(UpdatePaymentStatusRequest.StatusEnum.FAILED);

        // Then - Should return 409 Conflict
        mockMvc.perform(patch("/payments/" + paymentId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(409))
                .andExpect(jsonPath("$.message").value("Invalid status transition"))
                .andExpect(jsonPath("$.details").exists())
                .andExpect(jsonPath("$.details").value(org.hamcrest.Matchers.containsString("Cannot transition from terminal state")));
    }

    @Test
    void updatePaymentStatus_ErrorResponseFormat_ShouldIncludeCodeMessageAndDetails() throws Exception {
        // Given - Create a payment with FAILED status (terminal state)
        Payment payment = Payment.builder()
                .amount(BigDecimal.valueOf(50.00))
                .currency("EUR")
                .method("PAYPAL")
                .status(PaymentStatus.FAILED)
                .userId("user456")
                .orderId("order789")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        Payment savedPayment = paymentRepository.save(payment);
        Long paymentId = savedPayment.getId();

        // When - Attempt invalid transition from FAILED to APPROVED
        UpdatePaymentStatusRequest requestBody = new UpdatePaymentStatusRequest();
        requestBody.setStatus(UpdatePaymentStatusRequest.StatusEnum.APPROVED);

        // Then - Verify error response format includes code 409, message, and details
        mockMvc.perform(patch("/payments/" + paymentId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(409))
                .andExpect(jsonPath("$.message").value("Invalid status transition"))
                .andExpect(jsonPath("$.details").exists())
                .andExpect(jsonPath("$.details").isString())
                .andExpect(jsonPath("$.details").value(org.hamcrest.Matchers.containsString("FAILED")));
    }

    @Test
    void updatePaymentStatus_WithNonExistentPayment_ShouldReturn404() throws Exception {
        // Given - Non-existent payment ID
        Long nonExistentId = 999L;
        UpdatePaymentStatusRequest requestBody = new UpdatePaymentStatusRequest();
        requestBody.setStatus(UpdatePaymentStatusRequest.StatusEnum.APPROVED);

        // When/Then
        mockMvc.perform(patch("/payments/" + nonExistentId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("Payment not found"));
    }

    @Test
    void updatePaymentStatus_WithAllValidTransitions_ShouldSucceed() throws Exception {
        // Test PENDING -> FAILED
        Payment payment1 = Payment.builder()
                .amount(BigDecimal.valueOf(50.00))
                .currency("USD")
                .method("CREDIT_CARD")
                .status(PaymentStatus.PENDING)
                .userId("user1")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        Payment saved1 = paymentRepository.save(payment1);
        
        UpdatePaymentStatusRequest request1 = new UpdatePaymentStatusRequest();
        request1.setStatus(UpdatePaymentStatusRequest.StatusEnum.FAILED);
        
        mockMvc.perform(patch("/payments/" + saved1.getId() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FAILED"));

        // Test PENDING -> CANCELED
        Payment payment2 = Payment.builder()
                .amount(BigDecimal.valueOf(75.00))
                .currency("EUR")
                .method("PAYPAL")
                .status(PaymentStatus.PENDING)
                .userId("user2")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        Payment saved2 = paymentRepository.save(payment2);
        
        UpdatePaymentStatusRequest request2 = new UpdatePaymentStatusRequest();
        request2.setStatus(UpdatePaymentStatusRequest.StatusEnum.CANCELED);
        
        mockMvc.perform(patch("/payments/" + saved2.getId() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELED"));
    }

    @Test
    void listPayments_WithNoFilters_ShouldReturnPaginatedList() throws Exception {
        // Given - Create multiple payments
        for (int i = 0; i < 5; i++) {
            CreatePaymentRequest request = new CreatePaymentRequest();
            request.setAmount(10.0 + i);
            request.setCurrency("USD");
            request.setMethod(CreatePaymentRequest.MethodEnum.CREDIT_CARD);
            request.setUserId("user" + i);
            request.setOrderId(JsonNullable.of("order" + i));

            mockMvc.perform(post("/payments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        // When/Then
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/payments"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(5)))
                .andExpect(jsonPath("$.totalElements").value(org.hamcrest.Matchers.greaterThanOrEqualTo(5)))
                .andExpect(jsonPath("$.totalPages").exists())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20));
    }

    @Test
    void listPayments_WithStatusFilter_ShouldReturnFilteredPayments() throws Exception {
        // Given - Create payments with different statuses
        CreatePaymentRequest request1 = new CreatePaymentRequest();
        request1.setAmount(99.99);
        request1.setCurrency("USD");
        request1.setMethod(CreatePaymentRequest.MethodEnum.CREDIT_CARD);
        request1.setUserId("user123");
        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        // When/Then - Filter by PENDING status
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/payments?status=PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].status").value("PENDING"))
                .andExpect(jsonPath("$.totalElements").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));
    }

    @Test
    void listPayments_WithUserIdFilter_ShouldReturnFilteredPayments() throws Exception {
        // Given - Create payment for specific user
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setAmount(50.00);
        request.setCurrency("EUR");
        request.setMethod(CreatePaymentRequest.MethodEnum.PAYPAL);
        request.setUserId("filterUser");
        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // When/Then
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/payments?userId=filterUser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].userId").value("filterUser"))
                .andExpect(jsonPath("$.totalElements").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));
    }

    @Test
    void listPayments_WithOrderIdFilter_ShouldReturnFilteredPayments() throws Exception {
        // Given - Create payment with specific orderId
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setAmount(75.00);
        request.setCurrency("GBP");
        request.setMethod(CreatePaymentRequest.MethodEnum.BANK_TRANSFER);
        request.setUserId("user999");
        request.setOrderId(JsonNullable.of("filterOrder"));
        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // When/Then
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/payments?orderId=filterOrder"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].orderId").value("filterOrder"))
                .andExpect(jsonPath("$.totalElements").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));
    }

    @Test
    void listPayments_WithMultipleFilters_ShouldReturnFilteredPayments() throws Exception {
        // Given - Create payment with specific status and userId
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setAmount(100.00);
        request.setCurrency("USD");
        request.setMethod(CreatePaymentRequest.MethodEnum.CREDIT_CARD);
        request.setUserId("multiFilterUser");
        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // When/Then
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/payments?status=PENDING&userId=multiFilterUser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].status").value("PENDING"))
                .andExpect(jsonPath("$.content[0].userId").value("multiFilterUser"))
                .andExpect(jsonPath("$.totalElements").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));
    }

    @Test
    void listPayments_WithPagination_ShouldReturnPaginatedResults() throws Exception {
        // Given - Create multiple payments
        for (int i = 0; i < 15; i++) {
            CreatePaymentRequest request = new CreatePaymentRequest();
            request.setAmount(10.0 + i);
            request.setCurrency("USD");
            request.setMethod(CreatePaymentRequest.MethodEnum.CREDIT_CARD);
            request.setUserId("pagUser");
            mockMvc.perform(post("/payments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        // When/Then - First page with size 10
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/payments?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(10))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(org.hamcrest.Matchers.greaterThanOrEqualTo(15)));

        // When/Then - Second page
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/payments?page=1&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(10));
    }

    @Test
    void listPayments_WithInvalidSize_ShouldReturn400() throws Exception {
        // When/Then
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/payments?size=200"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details").value("Page size must be between 1 and 100"));
    }

    @Test
    void listPayments_WithInvalidPage_ShouldReturn400() throws Exception {
        // When/Then
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/payments?page=-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.details").value("Page number must be >= 0"));
    }

    @Test
    void listPayments_WithEmptyResult_ShouldReturnEmptyList() throws Exception {
        // Given - Clear all payments
        paymentRepository.deleteAll();

        // When/Then
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/payments?status=APPROVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));
    }
}

