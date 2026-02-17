package com.ticket.core.domain.performanceseat;

import com.ticket.core.enums.PerformanceSeatState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PerformanceSeatService {
    private final PerformanceSeatRepository performanceSeatRepository;

    public List<PerformanceSeat> getAllAvailableSeats() {
        return performanceSeatRepository.findAllByStateEquals(PerformanceSeatState.AVAILABLE);
//        availableSeats.stream()
    }
}
