package com.example.coffeeordersystem.global.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER-404", "존재하지 않는 사용자입니다."),

    // Menu
    MENU_NOT_FOUND(HttpStatus.NOT_FOUND, "MENU-404", "존재하지 않는 메뉴입니다."),

    // Point
    POINT_NOT_FOUND(HttpStatus.NOT_FOUND, "POINT-404", "포인트 정보가 존재하지 않습니다."),
    INVALID_CHARGE_AMOUNT(HttpStatus.BAD_REQUEST, "POINT-400", "충전 금액은 0보다 커야 합니다."),
    INVALID_POINT_BALANCE(HttpStatus.BAD_REQUEST, "POINT-401", "포인트 잔액은 0 이상이어야 합니다."),
    INSUFFICIENT_POINT(HttpStatus.BAD_REQUEST, "POINT-402", "포인트 잔액이 부족합니다."),

    // Order
    INVALID_PAYMENT_AMOUNT(HttpStatus.BAD_REQUEST, "ORDER-400", "주문 결제 금액이 올바르지 않습니다."),

    // External
    DATA_PLATFORM_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "DATA-500", "데이터 수집 플랫폼 전송에 실패했습니다."),

    // Common
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "COMMON-400", "잘못된 요청입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON-500", "서버 내부 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
