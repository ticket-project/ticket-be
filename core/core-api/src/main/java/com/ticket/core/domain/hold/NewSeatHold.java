package com.ticket.core.domain.hold;

import java.util.List;

public class NewSeatHold {

    private final Long performanceId;
    private final List<Long> seatIds;

    public NewSeatHold(final Long performanceId, final List<Long> seatIds) {
        this.performanceId = performanceId;
        this.seatIds = List.copyOf(seatIds);
    }

    public Long getPerformanceId() {
        return performanceId;
    }

    public List<Long> getSeatIds() {
        return seatIds;
    }
}
