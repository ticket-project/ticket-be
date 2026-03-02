package com.ticket.core.domain.performanceseat;

import java.time.LocalDateTime;

/**
 * WebSocket으로 전달되는 좌석 상태 변경 메시지 DTO.
 */
public record SeatStatusMessage(
        Long performanceId,
        Long seatId,
        SeatAction action,
        Long memberId,
        LocalDateTime timestamp
) {

    public enum SeatAction {
        SELECTED,
        DESELECTED
    }

    public static SeatStatusMessage selected(final Long performanceId, final Long seatId, final Long memberId) {
        return new SeatStatusMessage(performanceId, seatId, SeatAction.SELECTED, memberId, LocalDateTime.now());
    }

    public static SeatStatusMessage deselected(final Long performanceId, final Long seatId, final Long memberId) {
        return new SeatStatusMessage(performanceId, seatId, SeatAction.DESELECTED, memberId, LocalDateTime.now());
    }
}
