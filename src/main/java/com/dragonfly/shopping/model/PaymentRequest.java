package com.dragonfly.shopping.model;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentRequest(
    String requestId,
    String payerAccountNumber,
    BigDecimal amount,
    String paymentType,
    String replyTopic,
    Instant timestamp
) {
    public PaymentRequest {
        if (requestId == null || requestId.isBlank()) {
            throw new IllegalArgumentException("Request ID cannot be null or blank");
        }
        if (payerAccountNumber == null || payerAccountNumber.isBlank()) {
            throw new IllegalArgumentException("Payer account number cannot be null or blank");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        if (paymentType == null || paymentType.isBlank()) {
            throw new IllegalArgumentException("Payment type cannot be null or blank");
        }
        if (replyTopic == null || replyTopic.isBlank()) {
            throw new IllegalArgumentException("Reply topic cannot be null or blank");
        }
        if (timestamp == null) {
            throw new IllegalArgumentException("Timestamp cannot be null");
        }
    }
} 