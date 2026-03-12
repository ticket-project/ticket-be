package com.ticket.core.domain.order.command.usecase;

import com.ticket.core.aop.DistributedLock;
import com.ticket.core.domain.hold.event.HoldCreatedEvent;
import com.ticket.core.domain.hold.model.HoldSnapshot;
import com.ticket.core.domain.hold.support.HoldManager;
import com.ticket.core.domain.hold.support.HoldSeatAvailabilityValidator;
import com.ticket.core.domain.order.application.CreateOrderApplicationService;
import com.ticket.core.domain.order.model.Order;
import com.ticket.core.domain.order.repository.OrderRepository;
import com.ticket.core.domain.performance.Performance;
import com.ticket.core.domain.performance.PerformanceFinder;
import com.ticket.core.domain.performanceseat.model.PerformanceSeat;
import com.ticket.core.enums.OrderState;
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
public class StartOrderUseCase {

    private final PerformanceFinder performanceFinder;
    private final HoldSeatAvailabilityValidator holdSeatAvailabilityValidator;
    private final HoldManager holdManager;
    private final CreateOrderApplicationService createOrderApplicationService;
    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public record Input(Long performanceId, List<Long> seatIds, Long memberId) {}

    public record Output(String orderKey) {}

    @Transactional
    @DistributedLock(
            prefix = "pending-order",
            dynamicKey = "#input.memberId() + ':' + #input.performanceId()",
            message = "주문 시작 처리 중입니다. 잠시 후 다시 시도해 주세요."
    )
    public Output execute(final Input input) {
        final List<Long> seatIds = normalizeSeatIds(input.seatIds());
        final Performance performance = performanceFinder.findValidPerformanceById(input.performanceId());
        validateSeatCount(performance, seatIds);
        ensureNoPendingOrder(input.memberId(), input.performanceId());

        final List<PerformanceSeat> performanceSeats = holdSeatAvailabilityValidator.validate(input.performanceId(), seatIds);
        final HoldSnapshot snapshot = holdManager.createHold(
                input.memberId(),
                input.performanceId(),
                seatIds,
                Duration.ofSeconds(performance.getHoldTime())
        );
        applicationEventPublisher.publishEvent(new HoldCreatedEvent(snapshot));

        final Order order = createOrderApplicationService.createPendingOrder(
                input.memberId(),
                input.performanceId(),
                snapshot.holdKey(),
                snapshot.expiresAt(),
                performanceSeats
        );

        return new Output(order.getOrderKey());
    }

    private List<Long> normalizeSeatIds(final List<Long> requestedSeatIds) {
        if (requestedSeatIds.size() != new HashSet<>(requestedSeatIds).size()) {
            throw new CoreException(ErrorType.INVALID_REQUEST, "중복된 seatId가 포함되어 있습니다.");
        }

        final List<Long> seatIds = requestedSeatIds.stream().sorted().toList();
        if (seatIds.isEmpty()) {
            throw new CoreException(ErrorType.INVALID_REQUEST, "선점할 좌석이 없습니다.");
        }
        return seatIds;
    }

    private void validateSeatCount(final Performance performance, final List<Long> seatIds) {
        if (performance.isOverCount(seatIds.size())) {
            throw new CoreException(ErrorType.EXCEED_HOLD_LIMIT);
        }
    }

    private void ensureNoPendingOrder(final Long memberId, final Long performanceId) {
        final boolean alreadyExists = orderRepository.findByMemberIdAndPerformanceIdAndStatus(memberId, performanceId, OrderState.PENDING)
                .isPresent();
        if (alreadyExists) {
            throw new CoreException(ErrorType.PENDING_ORDER_ALREADY_EXISTS);
        }
    }
}
