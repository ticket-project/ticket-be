package com.ticket.core.enums;

public enum OrderSeatState {
    HELD("홀드 중"),
    CONFIRMED("주문 확정"),
    EXPIRED("주문 만료"),
    CANCELED("주문 취소");

    private final String description;

    OrderSeatState(final String description) {
        this.description = description;
    }

    public String getCode() {
        return name();
    }

    public String getDescription() {
        return description;
    }
}
