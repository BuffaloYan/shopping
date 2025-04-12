package com.dragonfly.shopping.config;

import com.dragonfly.shopping.controller.OrderControllerBlockingVT;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestConfiguration
public class TestConfig {
    
    @Bean
    @Primary
    public PaymentService mockPaymentService() {
        PaymentService mockService = mock(PaymentService.class);
        when(mockService.processPayment(any(PaymentRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(new PaymentResponse("SUCCESS", "INV123")));
        return mockService;
    }
    
    @Bean
    @Primary
    public KafkaTemplate<String, PaymentRequest> mockKafkaTemplate() {
        return mock(KafkaTemplate.class);
    }
    
    @Bean
    public WebTestClient webTestClient(OrderControllerBlockingVT controller) {
        return WebTestClient.bindToController(controller).build();
    }
} 