package com.ticket.core.enums;

public enum HoldState {
    EXPIRED("선점 만료"),
    ACTIVE("선점 활성"),
    FAILED("선점 실패"),
    PAID("결제 완료");

    private final String description;

    HoldState(final String description) {
        this.description = description;
    }

    public String getCode() {
        return name();
    }

    public String getDescription() {
        return description;
    }
}
