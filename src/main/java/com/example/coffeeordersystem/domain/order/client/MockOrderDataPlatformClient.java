package com.example.coffeeordersystem.domain.order.client;

import org.springframework.stereotype.Component;

@Component
public class MockOrderDataPlatformClient implements OrderDataPlatformClient {

    @Override
    public void send(OrderDataPlatformRequest request) {
        // 실제 외부 API 대신 과제 환경에서 호출 가능한 Mock 전송 지점으로 둠
    }
}
