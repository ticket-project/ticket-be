package com.ticket.storage.db.core;

import com.ticket.core.enums.PerformanceStatus;
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

    @Enumerated(value = EnumType.STRING)
    private PerformanceStatus status;

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

    public PerformanceStatus getStatus() {
        return status;
    }
}
