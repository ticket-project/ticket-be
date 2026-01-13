package com.ticket.core.domain.performanceseat;

import com.ticket.core.domain.performance.Performance;
import com.ticket.core.domain.seat.Seat;
import com.ticket.core.enums.PerformanceSeatState;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PerformanceSeatFinder {

    private final PerformanceSeatRepository performanceSeatRepository;

    public PerformanceSeatFinder(final PerformanceSeatRepository performanceSeatRepository) {
        this.performanceSeatRepository = performanceSeatRepository;
    }

    public List<PerformanceSeat> findAvailablePerformanceSeats(final List<Long> seatIds, final Long performanceId) {
        return performanceSeatRepository.findByPerformanceIdAndSeatIdInAndState(
                performanceId,
                seatIds,
                PerformanceSeatState.AVAILABLE
        );
    }

    public List<PerformanceSeat> findAllByPerformanceAndSeatIn(final Performance performance, final List<Seat> seats) {
        return performanceSeatRepository.findAllByPerformanceAndSeatIn(performance, seats);
    }
}
