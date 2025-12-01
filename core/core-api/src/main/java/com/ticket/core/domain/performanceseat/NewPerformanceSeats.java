package com.ticket.core.domain.performanceseat;

import java.util.List;

public class NewPerformanceSeats {

    private final Long memberId;
    private final Long performanceId;
    private final List<Long> seatIds;

    public NewPerformanceSeats(final Long memberId, final Long performanceId, final List<Long> seatIds) {
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
