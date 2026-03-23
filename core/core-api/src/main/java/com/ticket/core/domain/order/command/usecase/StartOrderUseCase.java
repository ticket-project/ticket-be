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

    public record Output(String orderKey) {}

    @Transactional
    @DistributedLock(
            prefix = "pending-order",
            dynamicKey = "#input.memberId() + ':' + #input.performanceId()",
            leaseTime = 15000L,
            message = "주문 시작 처리 중입니다. 잠시 후 다시 시도해 주세요."
    )
    public Output execute(final Input input) {
        final OrderSeatIds seatIds = OrderSeatIds.from(input.seatIds());
        final Performance performance = validateStartable(input, seatIds);
        final OrderStartDomainService.OrderStartResult result = startOrder(input, seatIds, performance);
        applicationEventPublisher.publishEvent(new HoldCreatedEvent(result.snapshot()));
        return new Output(result.orderKey());
    }

    private Performance validateStartable(final Input input, final OrderSeatIds seatIds) {
        memberFinder.findActiveMemberById(input.memberId());
        final Performance performance = performanceFinder.findValidPerformanceById(input.performanceId());
        validateSeatCount(performance, seatIds);
        ensureNoPendingOrder(input.memberId(), input.performanceId());
        return performance;
    }

    private OrderStartDomainService.OrderStartResult startOrder(
            final Input input,
            final OrderSeatIds seatIds,
            final Performance performance
    ) {
        return orderStartDomainService.start(
                input.memberId(),
                input.performanceId(),
                seatIds.values(),
                Duration.ofSeconds(performance.getHoldTime())
        );
    }

    private void validateSeatCount(final Performance performance, final OrderSeatIds seatIds) {
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
