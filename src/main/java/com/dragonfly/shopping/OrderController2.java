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
@RequestMapping("/api/orders/v2")
public class OrderController2 {
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    
    @PostMapping
    public CompletableFuture<ResponseEntity<OrderResponse>> createOrder(@Valid @RequestBody OrderRequest order) {
        logger.debug("Received order request: {}", order);
        return CompletableFuture.supplyAsync(() -> {
            if (order.products().isEmpty()) {
                logger.warn("Order request received with no products");
                return new OrderContext(
                    "N/A", 
                    null, 
                    "INVALID_REQUEST", 
                    "No products in the order"
                );
            }

            BigDecimal totalPrice = order.products().stream()
                .map(Product::price)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            String orderId = UUID.randomUUID().toString().substring(0, 12);
            logger.debug("Generated order ID: {}", orderId);
            
            return new OrderContext(orderId, totalPrice, "PAYMENT_SUCCESS", "Order processed successfully");
        })
        .thenCompose(context -> makePayment(order)
            .thenApply(paymentResponse -> {
                OrderResponse response = new OrderResponse(
                    context.orderId(), 
                    context.totalPrice(), 
                    context.status(),
                    context.description(),
                    paymentResponse.invoiceId()
                );
                logger.info("Order processed successfully: {}", response);
                return ResponseEntity.ok(response);
            })
        )
        .exceptionally(throwable -> {
            logger.error("Payment processing failed", throwable);
            return ResponseEntity.internalServerError()
                .body(new OrderResponse("N/A", null, "PAYMENT_FAILED", "Payment processing failed", "N/A"));
        });
    }

    /**
     * write a method which make a rest api call to the payment service
     * @param orderRequest
     * @return
     */
    public CompletableFuture<PaymentResponse> makePayment(OrderRequest orderRequest) {
        // try {
        //     Thread.sleep(200);
        // } catch (InterruptedException e) {
        //     Thread.currentThread().interrupt();
        //     logger.error("Order processing interrupted", e);
        // }

       return new CompletableFuture<PaymentResponse>()
            .completeOnTimeout(new PaymentResponse("PAYMENT_SUCCESS", "INV123"), 200, TimeUnit.MILLISECONDS);

    }

    private record OrderContext(String orderId, BigDecimal totalPrice, String status, String description) {}
}