package com.ticket.core.domain.order.application;

import com.ticket.core.domain.hold.finder.HoldHistoryFinder;
import com.ticket.core.domain.hold.model.HoldHistory;
import com.ticket.core.domain.order.domainservice.OrderLifecycleDomainService;
import com.ticket.core.domain.order.event.OrderCancelledEvent;
import com.ticket.core.domain.order.event.OrderExpiredEvent;
import com.ticket.core.domain.order.finder.OrderFinder;
import com.ticket.core.domain.order.finder.OrderSeatFinder;
import com.ticket.core.domain.order.model.Order;
import com.ticket.core.domain.order.model.OrderSeat;
import com.ticket.core.domain.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderLifecycleApplicationService {

    private final OrderFinder orderFinder;
    private final OrderRepository orderRepository;
    private final OrderSeatFinder orderSeatFinder;
    private final HoldHistoryFinder holdHistoryFinder;
    private final OrderLifecycleDomainService orderLifecycleDomainService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public void cancelPendingOrder(final String orderKey, final Long memberId, final LocalDateTime now) {
        final Order order = orderFinder.findPendingOwnedByOrderKey(orderKey, memberId);
        final OrderTransitionContext context = loadTransitionContext(order);
        orderLifecycleDomainService.cancel(order, context.orderSeats(), context.holdHistories(), now);
        applicationEventPublisher.publishEvent(new OrderCancelledEvent(order.getPerformanceId(), order.getHoldToken(), context.seatIds()));
    }

    @Transactional
    public void expirePendingOrder(final String orderKey, final LocalDateTime now) {
        final Order order = orderFinder.findByOrderKey(orderKey);
        expirePendingOrder(order, now);
    }

    @Transactional
    public void expirePendingOrder(final Order order, final LocalDateTime now) {
        if (!order.isPending()) {
            return;
        }

        final OrderTransitionContext context = loadTransitionContext(order);
        orderLifecycleDomainService.expire(order, context.orderSeats(), context.holdHistories(), now);
        applicationEventPublisher.publishEvent(new OrderExpiredEvent(order.getPerformanceId(), order.getHoldToken(), context.seatIds()));
    }

    @Transactional
    public void expirePendingOrderByHoldToken(final String holdToken, final LocalDateTime now) {
        final Order order = orderRepository.findByHoldToken(holdToken)
                .orElse(null);
        if (order == null) {
            return;
        }
        expirePendingOrder(order, now);
    }

    private OrderTransitionContext loadTransitionContext(final Order order) {
        final List<OrderSeat> orderSeats = orderSeatFinder.getOrderSeatsByOrderId(order.getId());
        final List<HoldHistory> holdHistories = holdHistoryFinder.findByHoldToken(order.getHoldToken());
        final List<Long> seatIds = orderSeats.stream()
                .map(OrderSeat::getSeatId)
                .toList();
        return new OrderTransitionContext(orderSeats, holdHistories, seatIds);
    }

    private record OrderTransitionContext(
            List<OrderSeat> orderSeats,
            List<HoldHistory> holdHistories,
            List<Long> seatIds
    ) {
    }
}
