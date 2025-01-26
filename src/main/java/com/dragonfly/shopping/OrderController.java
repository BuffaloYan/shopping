package com.dragonfly.shopping;

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

@RestController
@RequestMapping("/api/orders/v1")
public class OrderController {
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    
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

            PaymentResponse paymentResponse = makePayment(order).join();

            OrderResponse response = new OrderResponse(orderId, totalPrice, "PAYMENT_SUCCESS", "Order processed successfully", paymentResponse.invoiceId());
            logger.info("Order processed successfully: {}", response);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Payment processing failed", e);
            return ResponseEntity.internalServerError().body(new OrderResponse("N/A", null, "PAYMENT_FAILED", "Payment processing failed", "N/A"));
        }
    }

    public CompletableFuture<PaymentResponse> makePayment(OrderRequest orderRequest) {
        return new CompletableFuture<PaymentResponse>()
            .completeOnTimeout(new PaymentResponse("PAYMENT_SUCCESS", "INV123"), 200, TimeUnit.MILLISECONDS);
    }
}