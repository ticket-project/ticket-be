package com.ticket.core.domain.performanceseat;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PerformanceSeatFinder {

    private final PerformanceSeatRepository performanceSeatRepository;

    public List<PerformanceSeat> findByPerformanceIdAndSeatIdIn(final Long performanceId, final List<Long> seatIds) {
        return performanceSeatRepository.findAllByPerformanceIdAndSeatIdIn(performanceId, seatIds);
    }

}
