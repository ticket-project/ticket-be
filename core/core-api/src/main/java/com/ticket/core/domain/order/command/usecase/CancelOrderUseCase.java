package com.ticket.core.domain.order.command.usecase;

import com.ticket.core.domain.hold.finder.HoldHistoryFinder;
import com.ticket.core.domain.hold.model.HoldHistory;
import com.ticket.core.domain.order.domainservice.OrderLifecycleDomainService;
import com.ticket.core.domain.order.event.OrderCancelledEvent;
import com.ticket.core.domain.order.finder.OrderFinder;
import com.ticket.core.domain.order.finder.OrderSeatFinder;
import com.ticket.core.domain.order.model.Order;
import com.ticket.core.domain.order.model.OrderSeat;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CancelOrderUseCase {

    private final OrderFinder orderFinder;
    private final OrderSeatFinder orderSeatFinder;
    private final HoldHistoryFinder holdHistoryFinder;
    private final OrderLifecycleDomainService orderLifecycleDomainService;
    private final ApplicationEventPublisher applicationEventPublisher;

    public record Input(Long orderId, Long memberId) {}
    public record Output() {}

    @Transactional
    public Output execute(final Input input) {
        final Order order = orderFinder.findPendingOwnedById(input.orderId(), input.memberId());
        final List<OrderSeat> orderSeats = orderSeatFinder.getOrderSeatsByOrderId(order.getId());
        final List<HoldHistory> holdHistories = holdHistoryFinder.findByHoldToken(order.getHoldToken());
        final List<Long> seatIds = orderSeats.stream().map(OrderSeat::getSeatId).toList();

        final LocalDateTime now = LocalDateTime.now();
        orderLifecycleDomainService.cancel(order, orderSeats, holdHistories, now);
        applicationEventPublisher.publishEvent(new OrderCancelledEvent(order.getPerformanceId(), order.getHoldToken(), seatIds));
        return new Output();
    }
}
