package com.ticket.storage.db.core;

import com.ticket.core.enums.PerformanceState;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "PERFORMANCE")
public class PerformanceEntity extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long showId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private LocalDateTime reserveOpenTime;

    private LocalDateTime reserveCloseTime;

    private int maxCanReserveCount;

    @Enumerated(value = EnumType.STRING)
    private PerformanceState state;

    protected PerformanceEntity() {}

    public PerformanceEntity(final Long showId, final LocalDateTime startTime, final LocalDateTime endTime, final LocalDateTime reserveOpenTime, final LocalDateTime reserveCloseTime, final int maxCanReserveCount, final PerformanceState state) {
        this.showId = showId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.reserveOpenTime = reserveOpenTime;
        this.reserveCloseTime = reserveCloseTime;
        this.maxCanReserveCount = maxCanReserveCount;
        this.state = state;
    }

    public Long getId() {
        return id;
    }

    public Long getShowId() {
        return showId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public LocalDateTime getReserveOpenTime() {
        return reserveOpenTime;
    }

    public LocalDateTime getReserveCloseTime() {
        return reserveCloseTime;
    }

    public PerformanceState getState() {
        return state;
    }

    public int getMaxCanReserveCount() {
        return maxCanReserveCount;
    }

    public boolean isOverCount(final long requestReserveCount) {
        return requestReserveCount > maxCanReserveCount;
    }
}
