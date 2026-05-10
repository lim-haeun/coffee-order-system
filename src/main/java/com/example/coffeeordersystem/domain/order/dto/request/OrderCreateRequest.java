package com.example.coffeeordersystem.domain.order.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderCreateRequest(
        @NotNull(message = "사용자 ID는 필수입니다.")
        @Positive(message = "사용자 ID는 0보다 커야 합니다.")
        Long userId,

        @NotNull(message = "메뉴 ID는 필수입니다.")
        @Positive(message = "메뉴 ID는 0보다 커야 합니다.")
        Long menuId
) {
}
