package com.example.coffeeordersystem.domain.order.service;

import com.example.coffeeordersystem.domain.menu.entity.Menu;
import com.example.coffeeordersystem.domain.menu.repository.MenuRepository;
import com.example.coffeeordersystem.domain.order.client.OrderDataPlatformClient;
import com.example.coffeeordersystem.domain.order.client.OrderDataPlatformRequest;
import com.example.coffeeordersystem.domain.order.dto.request.OrderCreateRequest;
import com.example.coffeeordersystem.domain.order.dto.response.OrderResponse;
import com.example.coffeeordersystem.domain.order.entity.OrderStatus;
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
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private PointHistoryRepository pointHistoryRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private FakeOrderDataPlatformClient fakeOrderDataPlatformClient;

    @BeforeEach
    void setUp() {
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        pointHistoryRepository.deleteAll();
        pointRepository.deleteAll();
        menuRepository.deleteAll();
        userRepository.deleteAll();
        fakeOrderDataPlatformClient.clear();
    }

    @Test
    void createOrderSuccess() {
        User user = userRepository.save(new User("test-user"));
        Menu menu = menuRepository.save(new Menu("Americano", 4500));
        pointRepository.save(new Point(user, 10000L));

        OrderResponse response = orderService.createOrder(new OrderCreateRequest(user.getId(), menu.getId()));

        assertThat(response.userId()).isEqualTo(user.getId());
        assertThat(response.menuId()).isEqualTo(menu.getId());
        assertThat(response.paymentAmount()).isEqualTo(4500L);
        assertThat(response.currentBalance()).isEqualTo(5500L);
        assertThat(response.status()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(orderRepository.findAll()).hasSize(1);
        assertThat(orderItemRepository.findAll()).hasSize(1);
    }

    @Test
    void createOrderThrowsInsufficientPointWhenBalanceIsNotEnough() {
        User user = userRepository.save(new User("test-user"));
        Menu menu = menuRepository.save(new Menu("Latte", 5000));
        pointRepository.save(new Point(user, 1000L));

        assertThatThrownBy(() -> orderService.createOrder(new OrderCreateRequest(user.getId(), menu.getId())))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INSUFFICIENT_POINT));

        assertThat(orderRepository.findAll()).isEmpty();
    }

    @Test
    void createOrderThrowsMenuNotFoundWhenMenuDoesNotExist() {
        User user = userRepository.save(new User("test-user"));
        pointRepository.save(new Point(user, 10000L));

        assertThatThrownBy(() -> orderService.createOrder(new OrderCreateRequest(user.getId(), 999L)))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MENU_NOT_FOUND));
    }

    @Test
    void createOrderDeductsPointBalance() {
        User user = userRepository.save(new User("test-user"));
        Menu menu = menuRepository.save(new Menu("Americano", 4500));
        pointRepository.save(new Point(user, 10000L));

        orderService.createOrder(new OrderCreateRequest(user.getId(), menu.getId()));

        Point point = pointRepository.findAll().get(0);
        assertThat(point.getBalance()).isEqualTo(5500L);
    }

    @Test
    void createOrderCreatesUsePointHistory() {
        User user = userRepository.save(new User("test-user"));
        Menu menu = menuRepository.save(new Menu("Americano", 4500));
        pointRepository.save(new Point(user, 10000L));

        orderService.createOrder(new OrderCreateRequest(user.getId(), menu.getId()));

        List<PointHistory> histories = pointHistoryRepository.findAll();
        assertThat(histories).hasSize(1);
        assertThat(histories.get(0).getUser().getId()).isEqualTo(user.getId());
        assertThat(histories.get(0).getAmount()).isEqualTo(4500L);
        assertThat(histories.get(0).getType()).isEqualTo(PointHistoryType.USE);
    }

    @Test
    void createOrderSendsOrderDataToDataPlatformAfterCommit() {
        User user = userRepository.save(new User("test-user"));
        Menu menu = menuRepository.save(new Menu("Americano", 4500));
        pointRepository.save(new Point(user, 10000L));

        orderService.createOrder(new OrderCreateRequest(user.getId(), menu.getId()));

        assertThat(fakeOrderDataPlatformClient.requests()).hasSize(1);
        OrderDataPlatformRequest request = fakeOrderDataPlatformClient.requests().get(0);
        assertThat(request.userId()).isEqualTo(user.getId());
        assertThat(request.menuId()).isEqualTo(menu.getId());
        assertThat(request.paymentAmount()).isEqualTo(4500L);
    }

    @Test
    void createOrderKeepsCorrectBalanceWhenSameUserOrdersConcurrently() throws Exception {
        User user = userRepository.save(new User("test-user"));
        Menu menu = menuRepository.save(new Menu("Americano", 1000));
        pointRepository.save(new Point(user, 5000L));

        int threadCount = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        List<Throwable> failures = new CopyOnWriteArrayList<>();

        try {
            for (int i = 0; i < threadCount; i++) {
                executorService.submit(() -> {
                    readyLatch.countDown();

                    try {
                        startLatch.await();
                        orderService.createOrder(new OrderCreateRequest(user.getId(), menu.getId()));
                    } catch (Throwable throwable) {
                        failures.add(throwable);
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }

            // 모든 주문을 동시에 시작해 Point row lock이 잔액 정합성을 지키는지 확인한다.
            readyLatch.await(5, TimeUnit.SECONDS);
            startLatch.countDown();

            boolean completed = doneLatch.await(10, TimeUnit.SECONDS);

            assertThat(completed).isTrue();
            assertThat(failures).isEmpty();
            assertThat(pointRepository.findAll().get(0).getBalance()).isEqualTo(0L);
            assertThat(orderRepository.findAll()).hasSize(threadCount);
            assertThat(pointHistoryRepository.findAll()).hasSize(threadCount);
        } finally {
            executorService.shutdownNow();
        }
    }

    @TestConfiguration
    static class OrderServiceTestConfig {

        @Bean
        @Primary
        FakeOrderDataPlatformClient fakeOrderDataPlatformClient() {
            return new FakeOrderDataPlatformClient();
        }
    }

    static class FakeOrderDataPlatformClient implements OrderDataPlatformClient {

        private final List<OrderDataPlatformRequest> requests = new CopyOnWriteArrayList<>();

        @Override
        public void send(OrderDataPlatformRequest request) {
            requests.add(request);
        }

        List<OrderDataPlatformRequest> requests() {
            return requests;
        }

        void clear() {
            requests.clear();
        }
    }
}
