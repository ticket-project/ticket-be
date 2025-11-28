package com.ticket.core.domain.performanceseat;

import com.ticket.core.domain.performance.Performance;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

public class PerformanceSeat {
    private final Performance performance;
    private final AtomicInteger performanceSeatCount;

    public PerformanceSeat(final Performance performance, final int performanceSeatCount) {
        this.performance = performance;
        this.performanceSeatCount = new AtomicInteger(performanceSeatCount);
    }

    public AtomicInteger getPerformanceSeatCount() {
        return performanceSeatCount;
    }

    private boolean canReservation() {
        return this.performanceSeatCount.get() > 0;
    }

    public boolean reserve(final Long memberId, final LocalDateTime now) {
        if (!canReservation()) return false;
        if (performance.isPastPerformance(now)) throw new CoreException(ErrorType.IS_PAST_PERFORMANCE);
        if (performance.notYetCanReserveTime(now)) throw new CoreException(ErrorType.NOT_YET_RESERVE_TIME);
        this.performanceSeatCount.decrementAndGet();
        return true;
    }
}
