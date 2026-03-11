package com.ticket.core.domain.hold.command.usecase;

import com.ticket.core.domain.hold.event.HoldCreatedEvent;
import com.ticket.core.domain.hold.model.HoldSnapshot;
import com.ticket.core.domain.hold.support.HoldRedisService;
import com.ticket.core.domain.hold.support.HoldSeatAvailabilityValidator;
import com.ticket.core.domain.order.application.CreateOrderApplicationService;
import com.ticket.core.domain.performance.Performance;
import com.ticket.core.domain.performance.PerformanceFinder;
import com.ticket.core.domain.performanceseat.model.PerformanceSeat;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CreateHoldUseCase {

    private final PerformanceFinder performanceFinder;
    private final HoldSeatAvailabilityValidator holdSeatAvailabilityValidator;
    private final HoldRedisService holdRedisService;
    private final CreateOrderApplicationService createOrderApplicationService;
    private final ApplicationEventPublisher applicationEventPublisher;

    public record Input(Long performanceId, List<Long> seatIds, Long memberId) {}
    public record Output(Long orderId) {}

    @Transactional
    public Output execute(final Input input) {
        final List<Long> requestedSeatIds = input.seatIds();
        if (requestedSeatIds.size() != new HashSet<>(requestedSeatIds).size()) {
            throw new CoreException(ErrorType.INVALID_REQUEST, "중복된 seatId가 포함되어 있습니다.");
        }

        final List<Long> seatIds = requestedSeatIds.stream().sorted().toList();
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
        applicationEventPublisher.publishEvent(new HoldCreatedEvent(snapshot));

        final Long orderId = createOrderApplicationService.createPendingOrder(
                input.memberId(),
                input.performanceId(),
                snapshot.holdToken(),
                snapshot.expiresAt(),
                performanceSeats
        );

        return new Output(orderId);
    }
}
