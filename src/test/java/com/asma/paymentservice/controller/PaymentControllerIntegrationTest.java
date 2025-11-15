package com.asma.paymentservice.controller;

import com.asma.paymentservice.dto.CreatePaymentRequest;
import com.asma.paymentservice.repository.PaymentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        request.setOrderId(org.openapitools.jackson.nullable.JsonNullable.of("order456"));

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
        String jsonRequest = """
            {
                "amount": 75.50,
                "currency": "USD",
                "method": "CREDIT_CARD",
                "userId": "user999",
                "orderId": "order789"
            }
            """;

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
        String jsonRequest = """
            {
                "amount": 100.00,
                "currency": "EUR",
                "method": "PAYPAL",
                "userId": "user888",
                "orderId": null
            }
            """;

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
}

