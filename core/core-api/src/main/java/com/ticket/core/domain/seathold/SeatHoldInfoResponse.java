package com.ticket.core.domain.seathold;

import com.ticket.core.enums.HoldState;

import java.util.List;

public class SeatHoldInfoResponse {
    private final Long performanceId;
    private final List<Long> performanceSeatId;
    private final Long memberId;
    private final HoldState state;

    private SeatHoldInfoResponse(final Long performanceId, final List<Long> performanceSeatId, final Long memberId, final HoldState state) {
        this.performanceId = performanceId;
        this.performanceSeatId = performanceSeatId;
        this.memberId = memberId;
        this.state = state;
    }

    public static SeatHoldInfoResponse from(final SeatHoldInfo seatHoldInfo) {
        return new SeatHoldInfoResponse(seatHoldInfo.getPerformanceId(), seatHoldInfo.getPerformanceSeatId(), seatHoldInfo.getMemberId(), seatHoldInfo.getState());
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
