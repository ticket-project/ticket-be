package com.ticket.core.domain.performance;

import com.ticket.core.domain.BaseEntity;
import com.ticket.core.domain.show.Show;
import com.ticket.core.enums.PerformanceState;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "PERFORMANCE")
public class Performance extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Show show;

    private Long roundNo;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private LocalDateTime reserveOpenTime;

    private LocalDateTime reserveCloseTime;

    private int maxCanReserveCount;

    @Column(nullable = false)
    private Integer holdTime = 300;

    @Enumerated(value = EnumType.STRING)
    private PerformanceState state;

    protected Performance() {}

    public Performance(final Show show, final Long roundNo, final LocalDateTime startTime, final LocalDateTime endTime, final LocalDateTime reserveOpenTime, final LocalDateTime reserveCloseTime, final int maxCanReserveCount, final Integer holdTime, final PerformanceState state) {
        this.show = show;
        this.roundNo = roundNo;
        this.startTime = startTime;
        this.endTime = endTime;
        this.reserveOpenTime = reserveOpenTime;
        this.reserveCloseTime = reserveCloseTime;
        this.maxCanReserveCount = maxCanReserveCount;
        this.holdTime = holdTime;
        this.state = state;
    }

    public Long getId() {
        return id;
    }

    public Show getShow() {
        return show;
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

    public Long getRoundNo() {
        return roundNo;
    }

    public Integer getHoldTime() {
        return holdTime;
    }

    public boolean isOverCount(final long requestReserveCount) {
        return requestReserveCount > maxCanReserveCount;
    }
}
