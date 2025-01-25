package com.dragonfly.shopping.model;

import java.util.List;
import jakarta.validation.constraints.NotNull;

public record OrderRequest(
    @NotNull
    String customerId,
    @NotNull
    List<Product> products
) {} 