package com.ticket.storage.db.core;

import com.ticket.core.enums.PerformanceSeatState;
import jakarta.persistence.*;

@Entity
@Table(name = "PERFORMANCE_SEAT")
public class PerformanceSeatEntity extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long performanceId;

    private Long seatId;

    @Enumerated(EnumType.STRING)
    private PerformanceSeatState state;

    protected PerformanceSeatEntity() {}

    public PerformanceSeatEntity(final Long performanceId, final Long seatId, final PerformanceSeatState state) {
        this.performanceId = performanceId;
        this.seatId = seatId;
        this.state = state;
    }

    public Long getId() {
        return id;
    }

    public Long getPerformanceId() {
        return performanceId;
    }

    public Long getSeatId() {
        return seatId;
    }

    public PerformanceSeatState getState() {
        return state;
    }

    public void reserve() {
        this.state = PerformanceSeatState.RESERVED;
    }
}
