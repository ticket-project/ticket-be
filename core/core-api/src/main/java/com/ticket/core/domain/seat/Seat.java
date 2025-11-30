package com.ticket.core.domain.seat;

import com.ticket.storage.db.core.SeatStatus;

public class Seat {

    private final Long id;

    private Long performanceId;

    //행
    private final String x;

    //열
    private final String y;

    private SeatStatus status;

    public Seat(final Long id, final Long performanceId, final String x, final String y, final SeatStatus status) {
        this.id = id;
        this.performanceId = performanceId;
        this.x = x;
        this.y = y;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getX() {
        return x;
    }

    public String getY() {
        return y;
    }

    public Long getPerformanceId() {
        return performanceId;
    }

    public SeatStatus getStatus() {
        return status;
    }
}
