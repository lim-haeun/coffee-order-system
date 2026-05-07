package com.example.coffeeordersystem.domain.menu.controller;

import com.example.coffeeordersystem.domain.menu.entity.Menu;
import com.example.coffeeordersystem.domain.menu.repository.MenuRepository;
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

    @BeforeEach
    void setUp() {
        menuRepository.deleteAll();
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
        // 메뉴가 없는 경우에도 200 OK와 빈 배열을 반환해야 한다.
        mockMvc.perform(get("/api/menus"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("메뉴 목록 조회에 성공했습니다."))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }
}
