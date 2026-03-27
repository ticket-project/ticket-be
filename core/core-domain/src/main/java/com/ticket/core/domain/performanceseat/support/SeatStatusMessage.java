package com.ticket.core.domain.performanceseat.support;

import java.io.Serializable;
import java.time.LocalDateTime;

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

    public static SeatStatusMessage of(
            final Long performanceId,
            final Long seatId,
            final SeatAction action,
            final LocalDateTime timestamp
    ) {
        return new SeatStatusMessage(performanceId, seatId, action, timestamp);
    }
}
