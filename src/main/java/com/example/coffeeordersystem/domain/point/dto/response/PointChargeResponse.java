package com.example.coffeeordersystem.domain.point.dto.response;

public record PointChargeResponse(
        Long userId,
        Long chargedAmount,
        Long currentBalance
) {
}
