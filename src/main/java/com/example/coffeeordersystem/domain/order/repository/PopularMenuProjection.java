package com.example.coffeeordersystem.domain.order.repository;

public interface PopularMenuProjection {
    Long getMenuId();

    String getName();

    Long getPrice();

    Long getOrderCount();
}
