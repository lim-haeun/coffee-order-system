package com.example.coffeeordersystem.domain.point.controller;

import com.example.coffeeordersystem.domain.point.dto.request.PointChargeRequest;
import com.example.coffeeordersystem.domain.point.dto.response.PointChargeResponse;
import com.example.coffeeordersystem.domain.point.service.PointService;
import com.example.coffeeordersystem.global.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/points")
public class PointController {

    private final PointService pointService;

    public PointController(PointService pointService) {
        this.pointService = pointService;
    }

    // 포인트 충전
    @PostMapping("/charge")
    public ApiResponse<PointChargeResponse> chargePoint(@Valid @RequestBody PointChargeRequest request) {
        return ApiResponse.success(pointService.chargePoint(request), "포인트 충전에 성공했습니다.");
    }
}
