package com.example.coffeeordersystem.domain.order.client;

public record OrderDataPlatformRequest(
        Long userId,
        Long menuId,
        Long paymentAmount
) {
}
