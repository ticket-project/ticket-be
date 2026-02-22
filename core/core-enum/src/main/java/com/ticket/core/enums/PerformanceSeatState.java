package com.ticket.core.enums;

public enum PerformanceSeatState {
    AVAILABLE("예약 가능"),
    HELD("선점됨");

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
