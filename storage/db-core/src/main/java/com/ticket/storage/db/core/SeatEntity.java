package com.ticket.storage.db.core;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class SeatEntity {

    @Id @GeneratedValue
    private Long id;

    private Long performanceId;

    //행
    private String x;

    //열
    private String y;

    private SeatStatus status;

    public SeatEntity(final Long id, final Long performanceId, final String x, final String y, final SeatStatus status) {
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
