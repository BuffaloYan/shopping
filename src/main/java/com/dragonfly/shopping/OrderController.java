package com.dragonfly.shopping;

import com.dragonfly.shopping.model.OrderRequest;
import com.dragonfly.shopping.model.OrderResponse;
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

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    
    @PostMapping
    public CompletableFuture<ResponseEntity<OrderResponse>> createOrder(@Valid @RequestBody OrderRequest order) {
        logger.debug("Received order request: {}", order);
        return CompletableFuture.supplyAsync(() -> {
            if (order.products().isEmpty()) {
                logger.warn("Order request received with no products");
                return ResponseEntity.badRequest().body(new OrderResponse("INVALID_REQUEST", null, "No products in the order"));
            }

            BigDecimal totalPrice = order.products().stream()
                .map(Product::price)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            logger.debug("Calculated total price: {}", totalPrice);

            String orderId = UUID.randomUUID().toString().substring(0, 12);
            logger.debug("Generated order ID: {}", orderId);

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("Order processing interrupted", e);
            }

            OrderResponse response = new OrderResponse(orderId, totalPrice, "PAYMENT_SUCCESS");
            logger.info("Order processed successfully: {}", response);
            return ResponseEntity.ok(response);
        });
    }
}