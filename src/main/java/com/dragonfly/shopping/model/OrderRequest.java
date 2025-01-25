package com.dragonfly.shopping.model;

import java.util.List;
import jakarta.validation.constraints.NotEmpty;

public record OrderRequest(
    String customerId,
    @NotEmpty
    List<Product> products
) {} 