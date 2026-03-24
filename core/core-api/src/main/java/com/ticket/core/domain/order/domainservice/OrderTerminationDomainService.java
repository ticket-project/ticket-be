package com.ticket.core.domain.order.domainservice;

import com.ticket.core.domain.hold.application.HoldHistoryRecorder;
import com.ticket.core.domain.order.finder.OrderSeatFinder;
import com.ticket.core.domain.order.model.Order;
import com.ticket.core.domain.order.model.OrderSeat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OrderTerminationDomainService {

    private final OrderSeatFinder orderSeatFinder;
    private final HoldHistoryRecorder holdHistoryRecorder;
    private final OrderLifecycleDomainService orderLifecycleDomainService;

    public OrderTerminationResult cancel(final Order order, final LocalDateTime now) {
        final OrderTransitionContext context = loadTransitionContext(order);
        orderLifecycleDomainService.cancel(order, context.orderSeats(), now);
        holdHistoryRecorder.recordCanceled(
                order.getMemberId(),
                order.getPerformanceId(),
                order.getHoldKey(),
                now,
                context.orderSeats()
        );
        return new OrderTerminationResult(order.getPerformanceId(), order.getHoldKey(), context.seatIds());
    }

    public Optional<OrderTerminationResult> expire(final Order order, final LocalDateTime now) {
        if (!order.isPending()) {
            return Optional.empty();
        }

        final OrderTransitionContext context = loadTransitionContext(order);
        orderLifecycleDomainService.expire(order, context.orderSeats(), now);
        holdHistoryRecorder.recordExpired(
                order.getMemberId(),
                order.getPerformanceId(),
                order.getHoldKey(),
                now,
                context.orderSeats()
        );
        return Optional.of(new OrderTerminationResult(order.getPerformanceId(), order.getHoldKey(), context.seatIds()));
    }

    private OrderTransitionContext loadTransitionContext(final Order order) {
        final List<OrderSeat> orderSeats = orderSeatFinder.getOrderSeatsByOrderId(order.getId());
        final List<Long> seatIds = orderSeats.stream()
                .map(OrderSeat::getSeatId)
                .toList();
        return new OrderTransitionContext(orderSeats, seatIds);
    }

    private record OrderTransitionContext(
            List<OrderSeat> orderSeats,
            List<Long> seatIds
    ) {
    }

    public record OrderTerminationResult(Long performanceId, String holdKey, List<Long> seatIds) {
    }
}
