package com.example.coffeeordersystem.domain.order.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderCreateRequest(
        @NotNull @Positive Long userId,
        @NotNull @Positive Long menuId
) {
}
