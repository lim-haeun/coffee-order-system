package com.example.coffeeordersystem.domain.menu.service;

import com.example.coffeeordersystem.domain.menu.dto.response.MenuResponse;
import com.example.coffeeordersystem.domain.menu.dto.response.PopularMenuResponse;
import com.example.coffeeordersystem.domain.menu.entity.Menu;
import com.example.coffeeordersystem.domain.menu.repository.MenuRepository;
import com.example.coffeeordersystem.domain.order.entity.OrderStatus;
import com.example.coffeeordersystem.domain.order.repository.OrderItemRepository;
import com.example.coffeeordersystem.domain.order.repository.PopularMenuProjection;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class MenuService {

    private final MenuRepository menuRepository;
    private final OrderItemRepository orderItemRepository;

    public MenuService(MenuRepository menuRepository, OrderItemRepository orderItemRepository) {
        this.menuRepository = menuRepository;
        this.orderItemRepository = orderItemRepository;
    }

    public List<MenuResponse> getMenus() {
        // 응답 순서 고정을 위해 메뉴 ID 오름차순으로 조회
        return menuRepository.findAllByOrderByIdAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<PopularMenuResponse> getPopularMenus() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        // 최근 7일 동안 완료된 주문의 OrderItem을 DB에서 집계해 상위 3개만 조회
        return orderItemRepository.findPopularMenus(
                        sevenDaysAgo,
                        OrderStatus.COMPLETED,
                        PageRequest.of(0, 3)
                )
                .stream()
                .map(this::toPopularMenuResponse)
                .toList();
    }

    private MenuResponse toResponse(Menu menu) {
        return MenuResponse.from(menu.getId(), menu.getName(), menu.getPrice());
    }

    private PopularMenuResponse toPopularMenuResponse(PopularMenuProjection projection) {
        return new PopularMenuResponse(
                projection.getMenuId(),
                projection.getName(),
                projection.getPrice(),
                projection.getOrderCount()
        );
    }
}
