package com.ticket.core.domain.performanceseat.support;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * WebSocket으로 전달되는 좌석 상태 변경 메시지 DTO.
 * Redis Pub/Sub 전송을 위해 Serializable 구현.
 */
public record SeatStatusMessage(
        Long performanceId,
        Long seatId,
        SeatAction action,
        LocalDateTime timestamp
) implements Serializable {

    public enum SeatAction {
        SELECTED,
        DESELECTED,
        HELD,
        RELEASED,
        RESERVED
    }

    public static SeatStatusMessage of(Long performanceId, Long seatId, SeatAction action) {
        return new SeatStatusMessage(performanceId, seatId, action, LocalDateTime.now());
    }
}
