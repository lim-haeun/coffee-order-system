package com.example.coffeeordersystem.domain.order.client;

import com.example.coffeeordersystem.domain.order.event.OrderCompletedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class OrderDataPlatformEventListener {

    private final OrderDataPlatformClient orderDataPlatformClient;

    public OrderDataPlatformEventListener(OrderDataPlatformClient orderDataPlatformClient) {
        this.orderDataPlatformClient = orderDataPlatformClient;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendOrderData(OrderCompletedEvent event) {
        /*
         * 주문과 포인트 차감이 DB에 확정된 뒤 전송한다.
         * 외부 시스템 전송 책임을 Client로 분리해 실제 API와 Mock 구현을 쉽게 교체할 수 있다.
         */
        orderDataPlatformClient.send(new OrderDataPlatformRequest(
                event.userId(),
                event.menuId(),
                event.paymentAmount()
        ));
    }
}
