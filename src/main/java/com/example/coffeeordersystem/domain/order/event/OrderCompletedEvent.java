package com.example.coffeeordersystem.domain.order.event;

public record OrderCompletedEvent(
        Long userId,
        Long menuId,
        Long paymentAmount
) {
}
