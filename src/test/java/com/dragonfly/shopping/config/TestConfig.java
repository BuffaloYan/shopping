package com.dragonfly.shopping.config;

import com.dragonfly.shopping.controller.OrderControllerBlockingVT;
import com.dragonfly.shopping.controller.OrderControllerAsync;
import com.dragonfly.shopping.controller.OrderControllerBlocking;
import com.dragonfly.shopping.model.PaymentRequest;
import com.dragonfly.shopping.model.PaymentResponse;
import com.dragonfly.shopping.service.PaymentService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doAnswer;

@TestConfiguration
public class TestConfig {
    
    @Bean
    @Primary
    public PaymentService mockPaymentService(KafkaTemplate<String, PaymentRequest> kafkaTemplate) {
        PaymentService mockService = mock(PaymentService.class);
        when(mockService.processPayment(any(PaymentRequest.class)))
            .thenAnswer(invocation -> {
                PaymentRequest request = invocation.getArgument(0);
                // Send to Kafka as part of the mock behavior
                kafkaTemplate.send("payment-requests", request.requestId(), request);
                return CompletableFuture.completedFuture(
                    new PaymentResponse("SUCCESS", request.requestId(), "INV123", null)
                );
            });
        return mockService;
    }
    
    @Bean
    @Primary
    public KafkaTemplate<String, PaymentRequest> mockKafkaTemplate() {
        return mock(KafkaTemplate.class);
    }
    
    @Bean
    public WebTestClient webTestClient(
            OrderControllerBlockingVT controllerVT,
            OrderControllerAsync controllerAsync,
            OrderControllerBlocking controllerBlocking) {
        return WebTestClient.bindToController(controllerVT, controllerAsync, controllerBlocking).build();
    }
} 