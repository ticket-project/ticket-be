package com.ticket.core.domain.performanceseat.support;

import com.ticket.core.domain.hold.support.HoldManager;
import com.ticket.core.domain.performanceseat.model.PerformanceSeat;
import com.ticket.core.domain.performanceseat.repository.PerformanceSeatRepository;
import com.ticket.core.enums.PerformanceSeatState;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SeatSelectionAvailabilityValidator {

    private final HoldManager holdManager;
    private final PerformanceSeatRepository performanceSeatRepository;

    public void validate(final Long performanceId, final Long seatId) {
        final List<PerformanceSeat> performanceSeats = performanceSeatRepository.findAllByPerformanceIdAndSeatIdIn(
                performanceId,
                List.of(seatId)
        );
        if (performanceSeats.size() != 1) {
            throw new CoreException(ErrorType.SEAT_MISMATCH_IN_PERFORMANCE);
        }

        final PerformanceSeat performanceSeat = performanceSeats.getFirst();
        if (performanceSeat.getState() != PerformanceSeatState.AVAILABLE) {
            throw new CoreException(ErrorType.NOT_EXIST_AVAILABLE_SEAT);
        }
        if (holdManager.isHeld(performanceId, seatId)) {
            throw new CoreException(ErrorType.SEAT_ALREADY_HOLD);
        }
    }
}
