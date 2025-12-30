package com.ticket.core.domain.hold;

import java.util.List;

public class NewSeatHold {

    private final Long memberId;
    private final Long performanceId;
    private final List<Long> seatIds;

    public NewSeatHold(final Long memberId, final Long performanceId, final List<Long> seatIds) {
        this.memberId = memberId;
        this.performanceId = performanceId;
        this.seatIds = List.copyOf(seatIds);
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
