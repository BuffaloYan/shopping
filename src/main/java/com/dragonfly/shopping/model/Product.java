package com.dragonfly.shopping.model;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record Product(
    @NotBlank(message = "Product ID is required")
    String productId,
    
    @NotBlank(message = "Product name is required")
    String name,
    
    @NotNull
    @PositiveOrZero(message = "Price must be zero or positive")
    BigDecimal price
) {}
