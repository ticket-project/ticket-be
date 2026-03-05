package com.ticket.core.enums;

/**
 * 좌석 상태 API 응답 전용 enum.
 * DB 저장용 PerformanceSeatState와 분리하여, 클라이언트에게는 단순한 2가지 상태만 노출합니다.
 */
public enum SeatStatus {
    AVAILABLE("선택 가능"),
    OCCUPIED("사용 중");

    private final String description;

    SeatStatus(final String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
