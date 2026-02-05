package com.ticket.core.domain.show;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Region {
    SEOUL("서울"),
    GYEONGGI("경기"),
    INCHEON("인천"),
    GANGWON("강원"),
    CHUNGCHEONG("충청"),
    JEOLLA("전라"),
    GYEONGSANG("경상"),
    JEJU("제주");

    private final String description;

    Region(String description) {
        this.description = description;
    }

    @JsonValue
    public String getDescription() {
        return description;
    }

}
