package com.example.coffeeordersystem.domain.order.dto.response;

import com.example.coffeeordersystem.domain.order.entity.OrderStatus;

public record OrderResponse(
        Long orderId,
        Long userId,
        Long menuId,
        Long paymentAmount,
        Long currentBalance,
        OrderStatus status
) {
}
