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
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MenuService {

    private final MenuRepository menuRepository;
    private final OrderItemRepository orderItemRepository;

    public MenuService(MenuRepository menuRepository, OrderItemRepository orderItemRepository) {
        this.menuRepository = menuRepository;
        this.orderItemRepository = orderItemRepository;
    }

    // 메뉴 목록 조회
    public List<MenuResponse> getMenus() {
        return menuRepository.findAllByOrderByIdAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // 최근 7일 인기 메뉴 Top 3 조회
    public List<PopularMenuResponse> getPopularMenus() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        return orderItemRepository.findPopularMenus(
                        sevenDaysAgo,
                        OrderStatus.COMPLETED,
                        PageRequest.of(0, 3)
                )
                .stream()
                .map(this::toPopularMenuResponse)
                .toList();
    }

    // Menu 엔티티를 메뉴 목록 응답 DTO로 변환
    private MenuResponse toResponse(Menu menu) {
        return MenuResponse.from(menu.getId(), menu.getName(), menu.getPrice());
    }

    // 인기 메뉴 집계 결과를 응답 DTO로 변환
    private PopularMenuResponse toPopularMenuResponse(PopularMenuProjection projection) {
        return new PopularMenuResponse(
                projection.getMenuId(),
                projection.getName(),
                projection.getPrice(),
                projection.getOrderCount()
        );
    }
}
