package com.ticket.core.domain.hold.command.usecase;

import com.ticket.core.domain.hold.application.HoldReleaseApplicationService;
import com.ticket.core.domain.hold.model.HoldSnapshot;
import com.ticket.core.domain.hold.support.HoldRedisService;
import com.ticket.core.domain.hold.support.HoldSeatAvailabilityValidator;
import com.ticket.core.domain.order.application.CreateOrderApplicationService;
import com.ticket.core.domain.performance.Performance;
import com.ticket.core.domain.performance.PerformanceFinder;
import com.ticket.core.domain.performanceseat.application.SeatStatusPublishApplicationService;
import com.ticket.core.domain.performanceseat.command.SeatSelectionService;
import com.ticket.core.domain.performanceseat.model.PerformanceSeat;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
@Service
@RequiredArgsConstructor
public class CreateHoldUseCase {

    private final PerformanceFinder performanceFinder;
    private final HoldSeatAvailabilityValidator holdSeatAvailabilityValidator;
    private final HoldRedisService holdRedisService;
    private final HoldReleaseApplicationService holdReleaseApplicationService;
    private final CreateOrderApplicationService createOrderApplicationService;
    private final SeatSelectionService seatSelectionService;
    private final SeatStatusPublishApplicationService seatStatusPublishApplicationService;

    public record Input(Long performanceId, List<Long> seatIds, Long memberId) {}
    public record Output(Long orderId) {}

    @Transactional
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
        final HoldSnapshot snapshot = holdRedisService.createHold(
                input.memberId(),
                input.performanceId(),
                seatIds,
                Duration.ofSeconds(performance.getHoldTime())
        );

        try {
            final Long orderId = createOrderApplicationService.createPendingOrder(
                    input.memberId(),
                    input.performanceId(),
                    snapshot.holdToken(),
                    snapshot.expiresAt(),
                    performanceSeats
            );

            for (final Long seatId : seatIds) {
                seatSelectionService.forceDeselect(input.performanceId(), seatId);
            }
            seatStatusPublishApplicationService.publishHeld(input.performanceId(), seatIds);
            return new Output(orderId);
        } catch (final RuntimeException e) {
            holdReleaseApplicationService.releaseBySeatIds(snapshot.performanceId(), snapshot.holdToken(), snapshot.seatIds());
            throw e;
        }
    }
}
