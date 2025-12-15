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

    private Integer maxCanReserveCount;

    @Enumerated(value = EnumType.STRING)
    private PerformanceState state;

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

    public Integer getMaxCanReserveCount() {
        return maxCanReserveCount;
    }

    public boolean isOverCount(final long reservedCount) {
        return reservedCount > maxCanReserveCount;
    }
}
