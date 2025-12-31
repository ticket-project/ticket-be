package com.ticket.core.domain.performanceseat;

import com.ticket.core.enums.PerformanceSeatState;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PerformanceSeatService {
    private final PerformanceSeatRepository performanceSeatRepository;

    public PerformanceSeatService(final PerformanceSeatRepository performanceSeatRepository) {
        this.performanceSeatRepository = performanceSeatRepository;
    }

    public List<PerformanceSeat> getAllAvailableSeats() {
        return performanceSeatRepository.findAllByStateEquals(PerformanceSeatState.AVAILABLE);
//        availableSeats.stream()
    }
}
