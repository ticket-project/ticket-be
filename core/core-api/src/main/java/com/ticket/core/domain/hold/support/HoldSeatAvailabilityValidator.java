package com.ticket.core.domain.hold.support;

import com.ticket.core.domain.order.create.RequestedSeatIds;
import com.ticket.core.domain.performanceseat.finder.PerformanceSeatFinder;
import com.ticket.core.domain.performanceseat.model.PerformanceSeat;
import com.ticket.core.enums.PerformanceSeatState;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class HoldSeatAvailabilityValidator {

    private final PerformanceSeatFinder performanceSeatFinder;

    public List<PerformanceSeat> validate(final Long performanceId, final RequestedSeatIds requestedSeatIds) {
        final List<Long> seatIds = requestedSeatIds.toList();
        final List<PerformanceSeat> performanceSeats = performanceSeatFinder.findByPerformanceIdAndSeatIdIn(performanceId, seatIds);
        if (performanceSeats.size() != requestedSeatIds.size()) {
            throw new CoreException(ErrorType.SEAT_MISMATCH_IN_PERFORMANCE);
        }

        final boolean hasUnavailableSeat = performanceSeats.stream()
                .anyMatch(seat -> seat.getState() != PerformanceSeatState.AVAILABLE);
        if (hasUnavailableSeat) {
            throw new CoreException(ErrorType.NOT_EXIST_AVAILABLE_SEAT);
        }
        return performanceSeats;
    }
}
