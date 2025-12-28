package com.ticket.core.domain.seathold;

import com.ticket.core.enums.HoldState;

import java.util.List;

public class SeatHoldInfo {
    private final Long performanceId;
    private final List<Long> performanceSeatId;
    private final Long memberId;
    private final HoldState state;

    public SeatHoldInfo(final Long performanceId, final List<Long> performanceSeatId, final Long memberId, final HoldState state) {
        this.performanceId = performanceId;
        this.performanceSeatId = performanceSeatId;
        this.memberId = memberId;
//        this.expireAt = expireAt;
        this.state = state;
    }

    public Long getPerformanceId() {
        return performanceId;
    }

    public List<Long> getPerformanceSeatId() {
        return performanceSeatId;
    }

    public Long getMemberId() {
        return memberId;
    }

    public HoldState getState() {
        return state;
    }
}
