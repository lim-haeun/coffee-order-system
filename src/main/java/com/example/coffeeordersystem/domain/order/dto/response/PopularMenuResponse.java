package com.example.coffeeordersystem.domain.order.dto.response;

public record PopularMenuResponse(
        Long menuId,
        String menuName,
        long orderCount
) {
}
