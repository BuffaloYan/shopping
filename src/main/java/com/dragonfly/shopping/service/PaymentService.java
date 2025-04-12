package com.dragonfly.shopping.service;

import com.dragonfly.shopping.model.PaymentRequest;
import com.dragonfly.shopping.model.PaymentResponse;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Service interface for handling payment processing.
 */
@Service
public interface PaymentService {
    
    /**
     * Process a payment request asynchronously.
     *
     * @param request The payment request containing payment details
     * @return A CompletableFuture that will complete with the payment response
     */
    CompletableFuture<PaymentResponse> processPayment(PaymentRequest request);
    
    /**
     * Validates a payment request before processing.
     *
     * @param request The payment request to validate
     * @throws IllegalArgumentException if the request is invalid
     */
    default void validatePaymentRequest(PaymentRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Payment request cannot be null");
        }
        if (request.payerAccountNumber() == null || request.payerAccountNumber().isBlank()) {
            throw new IllegalArgumentException("Payer account number cannot be null or blank");
        }
        if (request.amount() == null || request.amount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        if (request.paymentType() == null || request.paymentType().isBlank()) {
            throw new IllegalArgumentException("Payment type cannot be null or blank");
        }
        if (request.replyTopic() == null || request.replyTopic().isBlank()) {
            throw new IllegalArgumentException("Reply topic cannot be null or blank");
        }
        if (request.timestamp() == null) {
            throw new IllegalArgumentException("Timestamp cannot be null");
        }
    }
} 