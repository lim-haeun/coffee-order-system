package com.example.coffeeordersystem.domain.point.service;

import com.example.coffeeordersystem.domain.point.dto.request.PointChargeRequest;
import com.example.coffeeordersystem.domain.point.dto.response.PointChargeResponse;
import com.example.coffeeordersystem.domain.point.entity.Point;
import com.example.coffeeordersystem.domain.point.entity.PointHistory;
import com.example.coffeeordersystem.domain.point.entity.PointHistoryType;
import com.example.coffeeordersystem.domain.point.repository.PointHistoryRepository;
import com.example.coffeeordersystem.domain.point.repository.PointRepository;
import com.example.coffeeordersystem.domain.order.repository.OrderItemRepository;
import com.example.coffeeordersystem.domain.order.repository.OrderRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class PointServiceTest {

    @Autowired
    private PointService pointService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private PointHistoryRepository pointHistoryRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @BeforeEach
    void setUp() {
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        pointHistoryRepository.deleteAll();
        pointRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void chargePointIncreasesBalance() {
        User user = userRepository.save(new User("test-user"));
        pointRepository.save(new Point(user, 5000L));

        PointChargeResponse response = pointService.chargePoint(new PointChargeRequest(user.getId(), 10000L));

        assertThat(response.userId()).isEqualTo(user.getId());
        assertThat(response.chargedAmount()).isEqualTo(10000L);
        assertThat(response.currentBalance()).isEqualTo(15000L);
    }

    @Test
    void chargePointThrowsUserNotFoundWhenUserDoesNotExist() {
        assertThatThrownBy(() -> pointService.chargePoint(new PointChargeRequest(999L, 10000L)))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND));
    }

    @Test
    void chargePointThrowsInvalidChargeAmountWhenAmountIsZeroOrLess() {
        User user = userRepository.save(new User("test-user"));

        assertThatThrownBy(() -> pointService.chargePoint(new PointChargeRequest(user.getId(), 0L)))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_CHARGE_AMOUNT));
    }

    @Test
    void chargePointCreatesChargeHistory() {
        User user = userRepository.save(new User("test-user"));

        pointService.chargePoint(new PointChargeRequest(user.getId(), 10000L));

        List<PointHistory> histories = pointHistoryRepository.findAll();
        assertThat(histories).hasSize(1);
        assertThat(histories.get(0).getUser().getId()).isEqualTo(user.getId());
        assertThat(histories.get(0).getAmount()).isEqualTo(10000L);
        assertThat(histories.get(0).getType()).isEqualTo(PointHistoryType.CHARGE);
        assertThat(histories.get(0).getCreatedAt()).isNotNull();
    }

    @Test
    void chargePointKeepsCorrectBalanceWhenSameUserChargesConcurrently() throws Exception {
        User user = userRepository.save(new User("test-user"));
        pointRepository.save(new Point(user, 0L));

        int threadCount = 5;
        long chargeAmount = 1000L;
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
                        pointService.chargePoint(new PointChargeRequest(user.getId(), chargeAmount));
                    } catch (Throwable throwable) {
                        failures.add(throwable);
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }

            // 모든 스레드가 같은 시점에 충전을 시작하도록 맞춰 락 동작을 검증한다.
            readyLatch.await(5, TimeUnit.SECONDS);
            startLatch.countDown();

            boolean completed = doneLatch.await(10, TimeUnit.SECONDS);

            assertThat(completed).isTrue();
            assertThat(failures).isEmpty();

            Point point = pointRepository.findAll().get(0);
            assertThat(point.getBalance()).isEqualTo(threadCount * chargeAmount);
            assertThat(pointHistoryRepository.findAll()).hasSize(threadCount);
        } finally {
            executorService.shutdownNow();
        }
    }
}
