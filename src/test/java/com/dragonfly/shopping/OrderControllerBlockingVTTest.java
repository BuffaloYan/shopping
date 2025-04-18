package com.dragonfly.shopping;

import com.dragonfly.shopping.model.OrderRequest;
import com.dragonfly.shopping.model.PaymentRequest;
import com.dragonfly.shopping.model.PaymentResponse;
import com.dragonfly.shopping.model.Product;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(com.dragonfly.shopping.config.TestConfig.class)
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })
@DirtiesContext
class OrderControllerBlockingVTTest {
    private static final Logger logger = LoggerFactory.getLogger(OrderControllerBlockingVTTest.class);

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private KafkaTemplate<String, PaymentRequest> kafkaTemplate;

    @Test
    void createOrder_WithValidRequest_ShouldReturnSuccess() throws Exception {
        Product product = new Product("PROD1", "Test Product", new BigDecimal("99.99"));
        OrderRequest request = new OrderRequest("CUST123", List.of(product));
        
        logger.info("Sending request: {}", objectMapper.writeValueAsString(request));

        webTestClient.post().uri("/api/orders/v3")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.orderId").exists()
                .jsonPath("$.totalPrice").isEqualTo("99.99")
                .jsonPath("$.status").isEqualTo("PAYMENT_SUCCESS")
                .jsonPath("$.description").isEqualTo("Order processed successfully")
                .jsonPath("$.invoiceId").isEqualTo("INV123");

        logger.info("Response received and validated");
    }

    @Test
    void createOrder_WithEmptyProducts_ShouldReturnBadRequest() throws Exception {
        OrderRequest request = new OrderRequest("CUST123", List.of());

        webTestClient.post().uri("/api/orders/v3")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.orderId").isEqualTo("N/A")
                .jsonPath("$.status").isEqualTo("INVALID_REQUEST")
                .jsonPath("$.description").isEqualTo("No products in the order")
                .jsonPath("$.invoiceId").isEqualTo("N/A");
    }

    @Test
    void createOrder_WithMultipleProducts_ShouldCalculateTotalPriceCorrectly() throws Exception {
        List<Product> products = List.of(
            new Product("PROD1", "Product 1", new BigDecimal("10.00")),
            new Product("PROD2", "Product 2", new BigDecimal("20.00"))
        );
        OrderRequest request = new OrderRequest("CUST123", products);

        webTestClient.post().uri("/api/orders/v3")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.totalPrice").isEqualTo("30.0")
                .jsonPath("$.status").isEqualTo("PAYMENT_SUCCESS")
                .jsonPath("$.description").isEqualTo("Order processed successfully")
                .jsonPath("$.invoiceId").isEqualTo("INV123");
    }

    @Test
    void createOrder_ShouldSendPaymentRequestToKafka() throws Exception {
        Product product = new Product("PROD1", "Test Product", new BigDecimal("99.99"));
        OrderRequest request = new OrderRequest("CUST123", List.of(product));

        webTestClient.post().uri("/api/orders/v3")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk();

        // Verify that the payment request was sent to Kafka
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(kafkaTemplate).send(eq("payment-requests"), any(String.class), any(PaymentRequest.class));
        });
    }
} 