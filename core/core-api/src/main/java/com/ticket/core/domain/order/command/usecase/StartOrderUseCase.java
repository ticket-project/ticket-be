package com.ticket.core.domain.order.command.usecase;

import com.ticket.core.aop.DistributedLock;
import com.ticket.core.domain.hold.event.HoldCreatedEvent;
import com.ticket.core.domain.member.MemberFinder;
import com.ticket.core.domain.order.domainservice.OrderStartDomainService;
import com.ticket.core.domain.order.repository.OrderRepository;
import com.ticket.core.domain.performance.Performance;
import com.ticket.core.domain.performance.PerformanceFinder;
import com.ticket.core.enums.OrderState;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StartOrderUseCase {

    private final MemberFinder memberFinder;
    private final PerformanceFinder performanceFinder;
    private final OrderStartDomainService orderStartDomainService;
    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public record Input(Long performanceId, List<Long> seatIds, Long memberId) {}

    public record Output(String orderKey, OrderState status, LocalDateTime expiresAt) {}

    @Transactional
    @DistributedLock(
            prefix = "pending-order",
            dynamicKey = "#input.memberId() + ':' + #input.performanceId()",
            leaseTime = 15000L,
            message = "주문 시작 처리 중입니다. 잠시 후 다시 시도해 주세요."
    )
    public Output execute(final Input input) {
        final RequestedSeatIds requestedSeatIds = RequestedSeatIds.from(input.seatIds());
        final Performance performance = validateStartable(input.memberId, input.performanceId, requestedSeatIds);
        final OrderStartDomainService.OrderResult result = startOrder(input.memberId, input.performanceId, requestedSeatIds, performance);
        applicationEventPublisher.publishEvent(new HoldCreatedEvent(result.snapshot()));
        return new Output(result.orderKey(), OrderState.PENDING, result.snapshot().expiresAt());
    }

    private Performance validateStartable(
            final Long memberId,
            final Long performanceId,
            final RequestedSeatIds requestedSeatIds
    ) {
        memberFinder.findActiveMemberById(memberId);
        final Performance performance = performanceFinder.findValidPerformanceById(performanceId);
        validateSeatCount(performance, requestedSeatIds);
        ensureNoPendingOrder(memberId, performanceId);
        return performance;
    }

    private OrderStartDomainService.OrderResult startOrder(
            final Long memberId,
            final Long performanceId,
            final RequestedSeatIds requestedSeatIds,
            final Performance performance
    ) {
        return orderStartDomainService.start(
                memberId,
                performanceId,
                requestedSeatIds.values(),
                Duration.ofSeconds(performance.getHoldTime())
        );
    }

    private void validateSeatCount(final Performance performance, final RequestedSeatIds requestedSeatIds) {
        if (performance.isOverCount(requestedSeatIds.size())) {
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
