package com.ticket.core.domain.show;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 지역 Enum
 * - JSON 직렬화 시 { "code": "SEOUL", "name": "서울" } 형태로 출력
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
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

    @JsonProperty("code")
    public String getCode() {
        return name();
    }

    @JsonProperty("name")
    public String getDescription() {
        return description;
    }
}
