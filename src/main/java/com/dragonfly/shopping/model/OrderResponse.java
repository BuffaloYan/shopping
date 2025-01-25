package com.dragonfly.shopping.model;

import java.math.BigDecimal;

public record OrderResponse(
    String orderId,
    BigDecimal totalPrice,
    String status,
    String description
) {} 