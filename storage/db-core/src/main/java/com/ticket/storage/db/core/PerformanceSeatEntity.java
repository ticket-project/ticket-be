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
        if (this.state == PerformanceSeatState.RESERVED) {
            throw new IllegalStateException("이미 예약된 좌석입니다."); //TODO 여기 예외 처리 어떻게?
        }
        this.state = PerformanceSeatState.RESERVED;
    }
}
