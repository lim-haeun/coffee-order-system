package com.example.coffeeordersystem.domain.menu.dto.response;

public record MenuResponse(
        Long menuId,
        String name,
        int price
) {
    public static MenuResponse from(Long menuId, String name, int price) {
        return new MenuResponse(menuId, name, price);
    }
}
