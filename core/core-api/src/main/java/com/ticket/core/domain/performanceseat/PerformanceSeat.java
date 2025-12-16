package com.ticket.core.domain.performanceseat;

import com.ticket.core.enums.PerformanceSeatState;

public class PerformanceSeat {
    private final Long performanceId;
    private final Long seatId;
    private final PerformanceSeatState state;

    public PerformanceSeat(final Long performanceId, final Long seatId, final PerformanceSeatState state) {
        this.performanceId = performanceId;
        this.seatId = seatId;
        this.state = state;
    }

    public Long getPerformanceId() {
        return performanceId;
    }

    public Long getSeatId() {
        return seatId;
    }

    public PerformanceSeatState getState() {
        return state;
    }
}
