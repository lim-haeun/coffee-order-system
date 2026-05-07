package com.example.coffeeordersystem.domain.point.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PointChargeRequest(
        @NotNull @Positive Long userId,
        @NotNull @Positive Long amount
) {
}
