package com.ticket.core.enums;

public enum PerformanceState {
    OPEN("공연 진행"),
    CLOSE("공연 종료");

    private final String description;

    PerformanceState(final String description) {
        this.description = description;
    }

    public String getCode() {
        return name();
    }

    public String getDescription() {
        return description;
    }
}
