package com.example.coffeeordersystem.domain.order.repository;

import com.example.coffeeordersystem.domain.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
