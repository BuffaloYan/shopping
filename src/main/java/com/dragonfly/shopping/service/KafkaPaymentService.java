package com.dragonfly.shopping.service;

import com.dragonfly.shopping.model.PaymentRequest;
import com.dragonfly.shopping.model.PaymentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class KafkaPaymentService implements PaymentService {
    private static final Logger logger = LoggerFactory.getLogger(KafkaPaymentService.class);
    private final KafkaTemplate<String, PaymentRequest> kafkaTemplate;
    private static final String PAYMENT_TOPIC = "payment-requests";

    public KafkaPaymentService(KafkaTemplate<String, PaymentRequest> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public CompletableFuture<PaymentResponse> processPayment(PaymentRequest request) {
        logger.info("Processing payment request: {}", request);
        try {
            validatePaymentRequest(request);
            kafkaTemplate.send(PAYMENT_TOPIC, request)

            String invoiceId = UUID.randomUUID().toString().substring(0, 6);
            return CompletableFuture.completedFuture(new PaymentResponse("SUCCESS", invoiceId));
        } catch (Exception e) {
            logger.error("Failed to process payment", e);
            return CompletableFuture.failedFuture(e);
        }
    }
} 