package com.ticket.core.enums;

public enum PerformanceSeatState {
    AVAILABLE("예매가능"),
    HELD("선점됨"),
    SELECTED("선택됨");

    private final String description;

    PerformanceSeatState(final String description) {
        this.description = description;
    }

    public String getCode() {
        return name();
    }

    public String getDescription() {
        return description;
    }
}
