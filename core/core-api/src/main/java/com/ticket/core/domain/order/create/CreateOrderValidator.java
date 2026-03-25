package com.ticket.core.domain.order.create;

import com.ticket.core.domain.member.MemberFinder;
import com.ticket.core.domain.order.repository.OrderRepository;
import com.ticket.core.domain.performance.Performance;
import com.ticket.core.domain.performance.PerformanceFinder;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class CreateOrderValidator {

    private final MemberFinder memberFinder;
    private final PerformanceFinder performanceFinder;
    private final OrderRepository orderRepository;

    public Performance validate(
            final Long memberId,
            final Long performanceId,
            final RequestedSeatIds requestedSeatIds,
            final LocalDateTime now
    ) {
        memberFinder.findActiveMemberById(memberId);
        final Performance performance = performanceFinder.findValidPerformanceById(performanceId, now);
        validateSeatCount(performance, requestedSeatIds);
        ensureNoPendingOrder(memberId, performanceId);
        return performance;
    }

    private void validateSeatCount(final Performance performance, final RequestedSeatIds requestedSeatIds) {
        if (!performance.isOverCount(requestedSeatIds.size())) {
            return;
        }
        throw new CoreException(ErrorType.EXCEED_HOLD_LIMIT);
    }

    private void ensureNoPendingOrder(final Long memberId, final Long performanceId) {
        if (!orderRepository.findByMemberIdAndPerformanceIdAndStatus(memberId, performanceId, com.ticket.core.enums.OrderState.PENDING).isPresent()) {
            return;
        }
        throw new CoreException(ErrorType.PENDING_ORDER_ALREADY_EXISTS);
    }
}
