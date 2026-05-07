package com.example.coffeeordersystem.domain.point.controller;

import com.example.coffeeordersystem.domain.point.repository.PointHistoryRepository;
import com.example.coffeeordersystem.domain.point.repository.PointRepository;
import com.example.coffeeordersystem.domain.user.entity.User;
import com.example.coffeeordersystem.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PointControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private PointHistoryRepository pointHistoryRepository;

    @BeforeEach
    void setUp() {
        pointHistoryRepository.deleteAll();
        pointRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void chargePointReturnsSuccessResponse() throws Exception {
        User user = userRepository.save(new User("테스트 사용자"));

        mockMvc.perform(post("/api/points/charge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": %d,
                                  "amount": 10000
                                }
                                """.formatted(user.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("포인트 충전에 성공했습니다."))
                .andExpect(jsonPath("$.data.userId").value(user.getId()))
                .andExpect(jsonPath("$.data.chargedAmount").value(10000L))
                .andExpect(jsonPath("$.data.currentBalance").value(10000L));

        assertThat(pointHistoryRepository.count()).isEqualTo(1);
    }

    @Test
    void chargePointReturnsBadRequestWhenAmountIsZero() throws Exception {
        User user = userRepository.save(new User("테스트 사용자"));

        mockMvc.perform(post("/api/points/charge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": %d,
                                  "amount": 0
                                }
                                """.formatted(user.getId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("충전 금액은 0보다 커야 합니다."));
    }

    @Test
    void chargePointReturnsNotFoundWhenUserDoesNotExist() throws Exception {
        mockMvc.perform(post("/api/points/charge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 999,
                                  "amount": 10000
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("사용자를 찾을 수 없습니다."));
    }
}