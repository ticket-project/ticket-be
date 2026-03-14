package com.ticket.core.domain.performanceseat.query.usecase;

import com.ticket.core.api.controller.response.SeatAvailabilityResponse;
import com.ticket.core.domain.hold.support.HoldManager;
import com.ticket.core.domain.performance.Performance;
import com.ticket.core.domain.performance.PerformanceFinder;
import com.ticket.core.domain.performanceseat.command.SeatSelectionService;
import com.ticket.core.domain.performanceseat.query.SeatAvailabilityCalculator;
import com.ticket.core.domain.performanceseat.query.SeatAvailabilityQueryRepository;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetSeatAvailabilityUseCase {

    private final PerformanceFinder performanceFinder;
    private final SeatAvailabilityQueryRepository seatAvailabilityQueryRepository;
    private final HoldManager holdManager;
    private final SeatSelectionService seatSelectionService;
    private final SeatAvailabilityCalculator seatAvailabilityCalculator;

    public record Input(Long performanceId) {}

    public record Output(SeatAvailabilityResponse availability) {}

    public Output execute(Input input) {
        final Performance performance = performanceFinder.findById(input.performanceId());

        if (performance.getShow() == null) {
            throw new CoreException(ErrorType.NOT_FOUND_DATA,
                    "회차와 연결된 공연을 찾을 수 없습니다. id=" + input.performanceId());
        }

        final SeatAvailabilityResponse response = seatAvailabilityCalculator.calculate(
                seatAvailabilityQueryRepository.findAvailableSeatRows(performance.getId(), performance.getShow().getId()),
                mergeRedisOccupiedIds(performance.getId())
        );

        return new Output(response);
    }

    private Set<Long> mergeRedisOccupiedIds(final Long performanceId) {
        final Set<Long> occupiedSeatIds = new HashSet<>(seatSelectionService.getSelectingSeatIds(performanceId));
        occupiedSeatIds.addAll(holdManager.getHoldingSeatIds(performanceId));
        return occupiedSeatIds;
    }
}
