package com.example.coffeeordersystem.domain.menu.controller;

import com.example.coffeeordersystem.domain.menu.dto.response.MenuResponse;
import com.example.coffeeordersystem.domain.menu.dto.response.PopularMenuResponse;
import com.example.coffeeordersystem.domain.menu.service.MenuService;
import com.example.coffeeordersystem.global.common.ApiResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/menus")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping
    public ApiResponse<List<MenuResponse>> getMenus() {
        // 인증 없이 전체 메뉴 목록을 조회
        return ApiResponse.success(menuService.getMenus(), "메뉴 목록 조회에 성공했습니다.");
    }

    @GetMapping("/popular")
    public ApiResponse<List<PopularMenuResponse>> getPopularMenus() {
        // 주문 성공 내역을 기준, 최근 7일간 가장 많이 주문된 메뉴를 조회
        return ApiResponse.success(menuService.getPopularMenus(), "인기 메뉴 목록 조회에 성공했습니다.");
    }
}
