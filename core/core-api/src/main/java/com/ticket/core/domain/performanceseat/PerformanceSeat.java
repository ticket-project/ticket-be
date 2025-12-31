package com.ticket.core.domain.performanceseat;

import com.ticket.core.enums.PerformanceSeatState;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "PERFORMANCE_SEAT")
public class PerformanceSeat {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long performanceId;

    private Long seatId;

    @Enumerated(EnumType.STRING)
    private PerformanceSeatState state;

    private LocalDateTime holdExpireAt;

    private Long holdByMemberId;

    private String holdToken;

    protected PerformanceSeat() {}

    public PerformanceSeat(final Long performanceId, final Long seatId, final PerformanceSeatState state, final LocalDateTime holdExpireAt, final Long holdByMemberId, final String holdToken) {
        this.performanceId = performanceId;
        this.seatId = seatId;
        this.state = state;
        this.holdExpireAt = holdExpireAt;
        this.holdByMemberId = holdByMemberId;
        this.holdToken = holdToken;
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

    public LocalDateTime getHoldExpireAt() {
        return holdExpireAt;
    }

    public Long getHoldByMemberId() {
        return holdByMemberId;
    }

    public String getHoldToken() {
        return holdToken;
    }

    public void reserve() {
        this.state = PerformanceSeatState.RESERVED;
    }

    public void hold(final Integer holdTime, final String holdToken) {
        this.state = PerformanceSeatState.HELD;
        this.holdExpireAt = LocalDateTime.now().plusSeconds(holdTime);
        this.holdToken = holdToken;
    }

    public void release() {
        this.state = PerformanceSeatState.AVAILABLE;
    }
}
