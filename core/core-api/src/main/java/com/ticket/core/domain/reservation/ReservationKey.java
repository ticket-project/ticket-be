package com.ticket.core.domain.reservation;

import com.ticket.storage.db.core.PerformanceSeatEntity;

import java.util.List;

public class ReservationKey {

    private final Long performanceId;
    private final List<PerformanceSeatEntity> performanceSeats;

    public ReservationKey(final Long performanceId, final List<PerformanceSeatEntity> performanceSeats) {
        this.performanceId = performanceId;
        this.performanceSeats = performanceSeats;
    }

    public Long getPerformanceId() {
        return performanceId;
    }

    public List<PerformanceSeatEntity> getPerformanceSeats() {
        return performanceSeats;
    }
}
