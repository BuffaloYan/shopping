package com.dragonfly.shopping.controller;

import com.dragonfly.shopping.model.OrderRequest;
import com.dragonfly.shopping.model.OrderResponse;
import com.dragonfly.shopping.model.PaymentResponse;
import com.dragonfly.shopping.model.Product;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.time.Instant;
import com.dragonfly.shopping.model.PaymentRequest;

@RestController
@RequestMapping("/api/orders/v1")
public class OrderControllerBlocking {
    private static final Logger logger = LoggerFactory.getLogger(OrderControllerBlocking.class);
    
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest order) {
        logger.debug("Received order request: {}", order);
        
        if (order.products().isEmpty()) {
            logger.warn("Order request received with no products");
            return ResponseEntity.badRequest().body(new OrderResponse("N/A", null, "INVALID_REQUEST", "No products in the order", "N/A"));
        }

        try {
            BigDecimal totalPrice = order.products().stream()
                .map(Product::price)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            logger.debug("Calculated total price: {}", totalPrice);

            String orderId = UUID.randomUUID().toString().substring(0, 12);
            logger.debug("Generated order ID: {}", orderId);

            String requestId = UUID.randomUUID().toString();
            PaymentResponse paymentResponse = new PaymentResponse("SUCCESS", requestId, "INV123", null);

            OrderResponse response = new OrderResponse(orderId, totalPrice, "PAYMENT_SUCCESS", "Order processed successfully", paymentResponse.getInvoiceId());
            logger.info("Order processed successfully: {}", response);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Payment processing failed", e);
            return ResponseEntity.internalServerError().body(new OrderResponse("N/A", null, "PAYMENT_FAILED", "Payment processing failed", "N/A"));
        }
    }

    public CompletableFuture<PaymentResponse> makePayment(OrderRequest orderRequest) {
        String requestId = UUID.randomUUID().toString();
        return new CompletableFuture<PaymentResponse>()
            .completeOnTimeout(new PaymentResponse("PAYMENT_SUCCESS", requestId, "INV123", null), 200, TimeUnit.MILLISECONDS);
    }

    private PaymentRequest createPaymentRequest(OrderRequest orderRequest) {
        BigDecimal totalAmount = orderRequest.products().stream()
            .map(Product::price)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        String requestId = UUID.randomUUID().toString();
        return new PaymentRequest(
            requestId,
            orderRequest.customerId(),
            totalAmount,
            "CREDIT_CARD", // Default payment type
            "payment-replies-" + requestId, // Unique reply topic per request
            Instant.now()
        );
    }

    private record OrderContext(String orderId, BigDecimal totalPrice, String status, String description) {}

    private OrderResponse createOrderResponse(OrderContext context) {
        return new OrderResponse(
            context.orderId(),
            context.totalPrice(),
            context.status(),
            context.description(),
            "INV123" // Default invoice ID for testing
        );
    }
}