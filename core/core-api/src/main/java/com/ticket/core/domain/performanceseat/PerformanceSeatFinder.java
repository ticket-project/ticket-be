package com.ticket.core.domain.performanceseat;

import com.ticket.core.enums.PerformanceSeatState;
import com.ticket.storage.db.core.PerformanceSeatEntity;
import com.ticket.storage.db.core.PerformanceSeatRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PerformanceSeatFinder {

    private final PerformanceSeatRepository performanceSeatRepository;

    public PerformanceSeatFinder(final PerformanceSeatRepository performanceSeatRepository) {
        this.performanceSeatRepository = performanceSeatRepository;
    }

    public List<PerformanceSeatEntity> findAvailablePerformanceSeats(final List<Long> seatIds, final Long performanceId) {
        return performanceSeatRepository.findByPerformanceIdAndSeatIdInAndState(
                performanceId,
                seatIds,
                PerformanceSeatState.AVAILABLE
        );
    }
}
