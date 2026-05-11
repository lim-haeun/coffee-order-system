package com.example.coffeeordersystem.domain.menu.controller;

import com.example.coffeeordersystem.domain.menu.entity.Menu;
import com.example.coffeeordersystem.domain.menu.repository.MenuRepository;
import com.example.coffeeordersystem.domain.order.entity.Order;
import com.example.coffeeordersystem.domain.order.entity.OrderItem;
import com.example.coffeeordersystem.domain.order.repository.OrderItemRepository;
import com.example.coffeeordersystem.domain.order.repository.OrderRepository;
import com.example.coffeeordersystem.domain.point.repository.PointHistoryRepository;
import com.example.coffeeordersystem.domain.point.repository.PointRepository;
import com.example.coffeeordersystem.domain.user.entity.User;
import com.example.coffeeordersystem.domain.user.repository.UserRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MenuControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MenuRepository menuRepository;

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
        menuRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void getMenusReturnsMenusInAscendingIdOrder() throws Exception {
        Menu americano = menuRepository.save(new Menu("아메리카노", 4500));
        Menu latte = menuRepository.save(new Menu("카페라떼", 5000));
        Menu vanillaLatte = menuRepository.save(new Menu("바닐라라떼", 5500));

        mockMvc.perform(get("/api/menus"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("메뉴 목록 조회에 성공했습니다."))
                .andExpect(jsonPath("$.data[0].menuId").value(americano.getId()))
                .andExpect(jsonPath("$.data[0].name").value("아메리카노"))
                .andExpect(jsonPath("$.data[0].price").value(4500))
                .andExpect(jsonPath("$.data[1].menuId").value(latte.getId()))
                .andExpect(jsonPath("$.data[1].name").value("카페라떼"))
                .andExpect(jsonPath("$.data[1].price").value(5000))
                .andExpect(jsonPath("$.data[2].menuId").value(vanillaLatte.getId()))
                .andExpect(jsonPath("$.data[2].name").value("바닐라라떼"))
                .andExpect(jsonPath("$.data[2].price").value(5500));
    }

    @Test
    void getMenusReturnsEmptyArrayWhenNoMenusExist() throws Exception {
        mockMvc.perform(get("/api/menus"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("메뉴 목록 조회에 성공했습니다."))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void getPopularMenusReturnsTopThreeMenusInLastSevenDays() throws Exception {
        User user = userRepository.save(new User("test-user"));
        Menu americano = menuRepository.save(new Menu("아메리카노", 4500));
        Menu latte = menuRepository.save(new Menu("카페라떼", 5000));
        Menu vanillaLatte = menuRepository.save(new Menu("바닐라라떼", 5500));
        Menu mocha = menuRepository.save(new Menu("카페모카", 6000));

        saveCompletedOrderItems(user, americano, 4, LocalDateTime.now().minusDays(1));
        saveCompletedOrderItems(user, latte, 3, LocalDateTime.now().minusDays(2));
        saveCompletedOrderItems(user, vanillaLatte, 2, LocalDateTime.now().minusDays(3));
        saveCompletedOrderItems(user, mocha, 1, LocalDateTime.now().minusDays(1));
        saveCompletedOrderItems(user, mocha, 10, LocalDateTime.now().minusDays(8));

        mockMvc.perform(get("/api/menus/popular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("인기 메뉴 목록 조회에 성공했습니다."))
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].menuId").value(americano.getId()))
                .andExpect(jsonPath("$.data[0].name").value("아메리카노"))
                .andExpect(jsonPath("$.data[0].price").value(4500L))
                .andExpect(jsonPath("$.data[0].orderCount").value(4L))
                .andExpect(jsonPath("$.data[1].menuId").value(latte.getId()))
                .andExpect(jsonPath("$.data[1].orderCount").value(3L))
                .andExpect(jsonPath("$.data[2].menuId").value(vanillaLatte.getId()))
                .andExpect(jsonPath("$.data[2].orderCount").value(2L));
    }

    @Test
    void getPopularMenusReturnsEmptyArrayWhenNoOrdersExist() throws Exception {
        mockMvc.perform(get("/api/menus/popular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("인기 메뉴 목록 조회에 성공했습니다."))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    private void saveCompletedOrderItems(User user, Menu menu, int count, LocalDateTime orderedAt) {
        for (int i = 0; i < count; i++) {
            Order order = orderRepository.save(new Order(user, Long.valueOf(menu.getPrice()), orderedAt));
            orderItemRepository.save(new OrderItem(order, menu, 1));
        }
    }
}
