package com.ticket.core.domain.reservation;

import java.util.List;

public class NewReservation {

    private final Long memberId;
    private final Long showId;
    private final Long performanceId;
    private final List<Long> seatIds;

    public NewReservation(final Long memberId, final Long showId, final Long performanceId, final List<Long> seatIds) {
        this.memberId = memberId;
        this.showId = showId;
        this.performanceId = performanceId;
        this.seatIds = seatIds;
    }

    public Long getMemberId() {
        return memberId;
    }

    public Long getShowId() {
        return showId;
    }

    public Long getPerformanceId() {
        return performanceId;
    }

    public List<Long> getSeatIds() {
        return seatIds;
    }
}
