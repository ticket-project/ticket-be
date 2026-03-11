package com.ticket.core.domain.hold.usecase;

import com.ticket.core.domain.hold.HoldCreator;
import com.ticket.core.domain.hold.HoldSeatAvailabilityValidator;
import com.ticket.core.domain.performance.Performance;
import com.ticket.core.domain.performance.PerformanceFinder;
import com.ticket.core.domain.performanceseat.PerformanceSeat;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CreateHoldUseCase {

    private final PerformanceFinder performanceFinder;
    private final HoldSeatAvailabilityValidator holdSeatAvailabilityValidator;
    private final HoldCreator holdCreator;

    public record Input(Long performanceId, List<Long> seatIds, Long memberId) {}
    public record Output(Long orderId) {}

    public Output execute(final Input input) {
        final List<Long> seatIds = input.seatIds().stream().distinct().sorted().toList();
        if (seatIds.isEmpty()) {
            throw new CoreException(ErrorType.INVALID_REQUEST, "선점할 좌석이 없습니다.");
        }

        final Performance performance = performanceFinder.findValidPerformanceById(input.performanceId());
        if (performance.isOverCount(seatIds.size())) {
            throw new CoreException(ErrorType.EXCEED_HOLD_LIMIT);
        }

        final List<PerformanceSeat> performanceSeats = holdSeatAvailabilityValidator.validate(input.performanceId(), seatIds);
        return new Output(holdCreator.create(
                input.memberId(),
                input.performanceId(),
                performance,
                seatIds,
                performanceSeats
        ));
    }
}
