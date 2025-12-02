package com.ticket.core.domain.performance;

import com.ticket.storage.db.core.PerformanceRepository;
import com.ticket.storage.db.core.SeatRepository;
import org.springframework.stereotype.Service;

@Service
public class PerformanceService {

    private final PerformanceRepository performanceRepository;
    private final SeatRepository seatRepository;

    public PerformanceService(final PerformanceRepository performanceRepository, final SeatRepository seatRepository) {
        this.performanceRepository = performanceRepository;
        this.seatRepository = seatRepository;
    }

}
