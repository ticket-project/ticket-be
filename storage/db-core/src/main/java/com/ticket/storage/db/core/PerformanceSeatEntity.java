package com.ticket.storage.db.core;

import com.ticket.core.enums.PerformanceSeatStatus;
import jakarta.persistence.*;

@Entity
@Table(name = "PERFORMANCE_SEAT")
public class PerformanceSeatEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long performanceId;

    private Long seatId;

    @Enumerated(EnumType.STRING)
    private PerformanceSeatStatus status;

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

    public PerformanceSeatStatus getStatus() {
        return status;
    }

    public void reserve() {
        if (this.status == PerformanceSeatStatus.RESERVED) {
            throw new IllegalStateException("이미 예약된 좌석입니다."); //TODO 여기 예외 처리 어떻게?
        }
        this.status = PerformanceSeatStatus.RESERVED;
    }
}
