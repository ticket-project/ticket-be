package com.ticket.core.domain.performanceseat;

import com.ticket.core.domain.BaseEntity;
import com.ticket.core.domain.performance.Performance;
import com.ticket.core.domain.seat.Seat;
import com.ticket.core.enums.PerformanceSeatState;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "PERFORMANCE_SEAT")
public class PerformanceSeat extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performance_id", nullable = false)
    private Performance performance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @Enumerated(EnumType.STRING)
    private PerformanceSeatState state;

    private BigDecimal price;

    protected PerformanceSeat() {}

    public PerformanceSeat(final Performance performance, final Seat seat, final PerformanceSeatState state, final BigDecimal price) {
        this.performance = performance;
        this.seat = seat;
        this.state = state;
        this.price = price;
    }

    public Long getId() {
        return id;
    }

    public Performance getPerformance() {
        return performance;
    }

    public Seat getSeat() {
        return seat;
    }

    public PerformanceSeatState getState() {
        return state;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void reserve() {
        this.state = PerformanceSeatState.HELD;
    }

    public void hold() {
        this.state = PerformanceSeatState.HELD;
    }

    public void release() {
        this.state = PerformanceSeatState.AVAILABLE;
    }
}
