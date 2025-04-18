package com.dragonfly.shopping.service;

import com.dragonfly.shopping.model.PaymentRequest;
import com.dragonfly.shopping.model.PaymentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class KafkaPaymentService implements PaymentService {
    private static final Logger logger = LoggerFactory.getLogger(KafkaPaymentService.class);
    private final KafkaTemplate<String, PaymentRequest> kafkaTemplate;
    private static final String PAYMENT_TOPIC = "payment-requests";
    private final ConcurrentHashMap<String, CompletableFuture<PaymentResponse>> pendingResponses = new ConcurrentHashMap<>();

    public KafkaPaymentService(KafkaTemplate<String, PaymentRequest> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public CompletableFuture<PaymentResponse> processPayment(PaymentRequest request) {
        logger.info("Processing payment request: {}", request);
        try {
            validatePaymentRequest(request);
            
            CompletableFuture<PaymentResponse> responseFuture = new CompletableFuture<>();
            pendingResponses.put(request.requestId(), responseFuture);
            
            // Send to the payment requests topic
            kafkaTemplate.send(PAYMENT_TOPIC, request.requestId(), request);
            
            // Set a timeout for the response
            responseFuture.completeOnTimeout(
                new PaymentResponse("TIMEOUT", request.requestId(), null, "Payment response timeout"),
                30, TimeUnit.SECONDS
            );
            
            return responseFuture;
        } catch (Exception e) {
            logger.error("Failed to process payment", e);
            return CompletableFuture.failedFuture(e);
        }
    }

    @KafkaListener(topics = "#{@environment.getProperty('kafka.topic.payment-replies')}-#{@environment.getProperty('spring.application.name')}-#{@environment.getProperty('server.port')}")
    public void receivePaymentResponse(@Payload PaymentResponse response, @Header(KafkaHeaders.RECEIVED_KEY) String requestId) {
        logger.info("Received payment response for request {}: {}", requestId, response);
        CompletableFuture<PaymentResponse> future = pendingResponses.remove(requestId);
        if (future != null) {
            future.complete(response);
        } else {
            logger.warn("Received response for unknown request ID: {}", requestId);
        }
    }
} 