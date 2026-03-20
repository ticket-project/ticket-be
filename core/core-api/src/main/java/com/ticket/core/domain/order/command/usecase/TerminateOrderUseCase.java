package com.ticket.core.domain.order.command.usecase;

import com.ticket.core.domain.member.MemberFinder;
import com.ticket.core.domain.order.domainservice.OrderTerminationDomainService;
import com.ticket.core.domain.order.event.OrderCancelledEvent;
import com.ticket.core.domain.order.event.OrderExpiredEvent;
import com.ticket.core.domain.order.finder.OrderFinder;
import com.ticket.core.domain.order.model.Order;
import com.ticket.core.domain.order.repository.OrderRepository;
import com.ticket.core.enums.OrderState;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TerminateOrderUseCase {

    private final MemberFinder memberFinder;
    private final OrderFinder orderFinder;
    private final OrderRepository orderRepository;
    private final OrderTerminationDomainService orderTerminationDomainService;
    private final ApplicationEventPublisher applicationEventPublisher;

    public record Input(String orderKey, Long memberId) {}

    @Transactional
    public void cancel(final Input input) {
        memberFinder.findActiveMemberById(input.memberId());
        final Order order = orderFinder.findPendingOwnedByOrderKeyForUpdate(input.orderKey(), input.memberId());
        final OrderTerminationDomainService.OrderTerminationResult result = orderTerminationDomainService.cancel(order, LocalDateTime.now());
        applicationEventPublisher.publishEvent(new OrderCancelledEvent(result.performanceId(), result.holdKey(), result.seatIds()));
    }

    @Transactional
    public void expireByOrderId(final Long orderId, final LocalDateTime now) {
        expire(findPendingOrder(orderId), now);
    }

    @Transactional
    public void expireByHoldKey(final String holdKey, final LocalDateTime now) {
        expire(findPendingOrder(holdKey), now);
    }

    private Order findPendingOrder(final Long orderId) {
        return orderRepository.findByIdAndStatusForUpdate(orderId, OrderState.PENDING)
                .orElse(null);
    }

    private Order findPendingOrder(final String holdKey) {
        return orderRepository.findByHoldKeyAndStatusForUpdate(holdKey, OrderState.PENDING)
                .orElse(null);
    }

    private void expire(final Order order, final LocalDateTime now) {
        if (order == null) {
            return;
        }
        final Optional<OrderTerminationDomainService.OrderTerminationResult> result = orderTerminationDomainService.expire(order, now);
        result.ifPresent(this::publishExpiredEvent);
    }

    private void publishExpiredEvent(final OrderTerminationDomainService.OrderTerminationResult result) {
        applicationEventPublisher.publishEvent(new OrderExpiredEvent(result.performanceId(), result.holdKey(), result.seatIds()));
    }
}
