package com.dragonfly.shopping;

import com.dragonfly.shopping.model.OrderRequest;
import com.dragonfly.shopping.model.Product;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class OrderControllerBlockingTest {
    private static final Logger logger = LoggerFactory.getLogger(OrderControllerBlockingTest.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createOrder_WithValidRequest_ShouldReturnSuccess() throws Exception {
        Product product = new Product("PROD1", "Test Product", new BigDecimal("99.99"));
        OrderRequest request = new OrderRequest("CUST123", List.of(product));
        
        logger.info("Sending request: {}", objectMapper.writeValueAsString(request));
        
        mockMvc.perform(post("/api/orders/v1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").exists())
                .andExpect(jsonPath("$.totalPrice").value("99.99"))
                .andExpect(jsonPath("$.status").value("PAYMENT_SUCCESS"))
                .andExpect(jsonPath("$.description").value("Order processed successfully"))
                .andExpect(jsonPath("$.invoiceId").value("INV123"));

        logger.info("Response received and validated");
    }

    @Test
    void createOrder_WithEmptyProducts_ShouldReturnBadRequest() throws Exception {
        OrderRequest request = new OrderRequest("CUST123", List.of());

        mockMvc.perform(post("/api/orders/v1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.orderId").value("N/A"))
                .andExpect(jsonPath("$.status").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.description").value("No products in the order"))
                .andExpect(jsonPath("$.invoiceId").value("N/A"));
    }

    @Test
    void createOrder_WithMultipleProducts_ShouldCalculateTotalPriceCorrectly() throws Exception {
        List<Product> products = List.of(
            new Product("PROD1", "Product 1", new BigDecimal("10.00")),
            new Product("PROD2", "Product 2", new BigDecimal("20.00"))
        );
        OrderRequest request = new OrderRequest("CUST123", products);

        mockMvc.perform(post("/api/orders/v1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.totalPrice").value("30.0"))
                .andExpect(jsonPath("$.status").value("PAYMENT_SUCCESS"))
                .andExpect(jsonPath("$.description").value("Order processed successfully"))
                .andExpect(jsonPath("$.invoiceId").value("INV123"));
    }
} 