package com.example.coffeeordersystem.domain.order.service;

import com.example.coffeeordersystem.domain.menu.entity.Menu;
import com.example.coffeeordersystem.domain.menu.repository.MenuRepository;
import com.example.coffeeordersystem.domain.order.dto.request.OrderCreateRequest;
import com.example.coffeeordersystem.domain.order.dto.response.OrderResponse;
import com.example.coffeeordersystem.domain.order.entity.Order;
import com.example.coffeeordersystem.domain.order.entity.OrderItem;
import com.example.coffeeordersystem.domain.order.event.OrderCompletedEvent;
import com.example.coffeeordersystem.domain.order.repository.OrderItemRepository;
import com.example.coffeeordersystem.domain.order.repository.OrderRepository;
import com.example.coffeeordersystem.domain.point.entity.Point;
import com.example.coffeeordersystem.domain.point.entity.PointHistory;
import com.example.coffeeordersystem.domain.point.entity.PointHistoryType;
import com.example.coffeeordersystem.domain.point.repository.PointHistoryRepository;
import com.example.coffeeordersystem.domain.point.repository.PointRepository;
import com.example.coffeeordersystem.domain.user.entity.User;
import com.example.coffeeordersystem.domain.user.repository.UserRepository;
import com.example.coffeeordersystem.global.exception.BusinessException;
import com.example.coffeeordersystem.global.exception.ErrorCode;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final UserRepository userRepository;
    private final MenuRepository menuRepository;
    private final PointRepository pointRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ApplicationEventPublisher eventPublisher;

    public OrderService(
            UserRepository userRepository,
            MenuRepository menuRepository,
            PointRepository pointRepository,
            PointHistoryRepository pointHistoryRepository,
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.userRepository = userRepository;
        this.menuRepository = menuRepository;
        this.pointRepository = pointRepository;
        this.pointHistoryRepository = pointHistoryRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public OrderResponse createOrder(OrderCreateRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Menu menu = menuRepository.findById(request.menuId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MENU_NOT_FOUND));

        /*
         * 포인트 row를 비관적 락으로 조회해 같은 사용자의 동시 주문이
         * 잔액 검증과 차감 사이를 끼어들지 못하게 한다.
         */
        Point point = pointRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.POINT_NOT_FOUND));

        Long paymentAmount = Long.valueOf(menu.getPrice());
        point.use(paymentAmount);

        pointHistoryRepository.save(new PointHistory(user, paymentAmount, PointHistoryType.USE));

        Order order = orderRepository.save(new Order(user, paymentAmount));
        orderItemRepository.save(new OrderItem(order, menu, 1));

        // 트랜잭션 커밋 이후 데이터 수집 플랫폼으로 주문 데이터를 전송한다.
        eventPublisher.publishEvent(new OrderCompletedEvent(user.getId(), menu.getId(), paymentAmount));

        return new OrderResponse(
                order.getId(),
                user.getId(),
                menu.getId(),
                paymentAmount,
                point.getBalance(),
                order.getStatus()
        );
    }
}
