package com.ticket.core.domain.seathold;

import com.ticket.core.enums.HoldState;

import java.util.List;

public class SeatHoldInfo {
    private final Long performanceId;
    private final List<Long> performanceSeatIds;
    private final Long memberId;
    private final HoldState state;

    public SeatHoldInfo(final Long performanceId, final List<Long> performanceSeatIds, final Long memberId, final HoldState state) {
        this.performanceId = performanceId;
        this.performanceSeatIds = performanceSeatIds;
        this.memberId = memberId;
        this.state = state;
    }

    public Long getPerformanceId() {
        return performanceId;
    }

    public List<Long> getPerformanceSeatIds() {
        return performanceSeatIds;
    }

    public Long getMemberId() {
        return memberId;
    }

    public HoldState getState() {
        return state;
    }
}
