package com.ticket.core.enums;

public enum SaleStatus {
    BEFORE_OPEN("판매 오픈 전"),
    ON_SALE("판매중"),
    CLOSED("판매 종료");

    private final String description;

    SaleStatus(final String description) {
        this.description = description;
    }

    public String getCode() {
        return name();
    }

    public String getDescription() {
        return description;
    }
}
