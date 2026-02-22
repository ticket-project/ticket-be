package com.ticket.core.enums;

public enum EntityStatus {
    ACTIVE("활성"),
    DELETED("삭제");

    private final String description;

    EntityStatus(final String description) {
        this.description = description;
    }

    public String getCode() {
        return name();
    }

    public String getDescription() {
        return description;
    }
}
