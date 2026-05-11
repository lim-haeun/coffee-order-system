package com.example.coffeeordersystem.domain.order.controller;

import com.example.coffeeordersystem.domain.order.dto.request.OrderCreateRequest;
import com.example.coffeeordersystem.domain.order.dto.response.OrderResponse;
import com.example.coffeeordersystem.domain.order.service.OrderService;
import com.example.coffeeordersystem.global.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ApiResponse<OrderResponse> createOrder(@Valid @RequestBody OrderCreateRequest request) {
        // 주문 생성과 포인트 결제를 하나의 서비스 트랜잭션에서 처리한다.
        return ApiResponse.success(orderService.createOrder(request), "주문이 완료되었습니다.");
    }
}
