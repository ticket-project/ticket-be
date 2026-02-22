package com.ticket.core.enums;

public enum OrderState {
    COMPLETED("주문 완료"),
    CANCELLED("주문 취소"),
    PENDING("결제 대기");

    private final String description;

    OrderState(final String description) {
        this.description = description;
    }

    public String getCode() {
        return name();
    }

    public String getDescription() {
        return description;
    }
}
