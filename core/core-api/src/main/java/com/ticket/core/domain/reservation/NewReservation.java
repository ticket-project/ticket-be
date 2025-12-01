package com.ticket.core.domain.reservation;

import java.util.List;

public class NewReservation {

    private final Long memberId;
    private final Long performanceId;
    private final List<Long> seatIds;

    public NewReservation(final Long memberId, final Long performanceId, final List<Long> seatIds) {
        this.memberId = memberId;
        this.performanceId = performanceId;
        this.seatIds = seatIds;
    }

    public Long getMemberId() {
        return memberId;
    }

    public Long getPerformanceId() {
        return performanceId;
    }

    public List<Long> getSeatIds() {
        return seatIds;
    }
}
