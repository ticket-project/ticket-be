package com.ticket.core.domain.performanceseat;

import com.ticket.core.domain.performance.Performance;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

public class PerformanceSeat {
    private final Long id;
    private final Long performanceId;
    private final Long seatId;
    private final AtomicInteger performanceSeatCount;

    public PerformanceSeat(final Long id, final Long performanceId, final Long seatId, final AtomicInteger performanceSeatCount) {
        this.id = id;
        this.performanceId = performanceId;
        this.seatId = seatId;
        this.performanceSeatCount = performanceSeatCount;
    }

    private boolean canReservation() {
        return this.performanceSeatCount.get() > 0;
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

    public AtomicInteger getPerformanceSeatCount() {
        return performanceSeatCount;
    }

    public boolean reserve(final Long memberId, final LocalDateTime now) {
        if (!canReservation()) return false;
        if (performance.isPastPerformance(now)) throw new CoreException(ErrorType.IS_PAST_PERFORMANCE);
        if (performance.notYetCanReserveTime(now)) throw new CoreException(ErrorType.NOT_YET_RESERVE_TIME);
        this.performanceSeatCount.decrementAndGet();
        return true;
    }
}
