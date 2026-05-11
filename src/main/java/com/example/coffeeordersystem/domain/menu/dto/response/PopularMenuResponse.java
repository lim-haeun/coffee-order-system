package com.example.coffeeordersystem.domain.menu.dto.response;

public record PopularMenuResponse(
        Long menuId,
        String name,
        Long price,
        Long orderCount
) {
}
