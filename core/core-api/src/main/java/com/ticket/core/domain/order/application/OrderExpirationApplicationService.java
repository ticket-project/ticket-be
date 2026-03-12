package com.ticket.core.domain.order.application;

import com.ticket.core.domain.hold.finder.HoldHistoryFinder;
import com.ticket.core.domain.hold.model.HoldHistory;
import com.ticket.core.domain.order.domainservice.OrderLifecycleDomainService;
import com.ticket.core.domain.order.event.OrderExpiredEvent;
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
public class OrderExpirationApplicationService {

    private final OrderSeatFinder orderSeatFinder;
    private final HoldHistoryFinder holdHistoryFinder;
    private final OrderLifecycleDomainService orderLifecycleDomainService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public void expire(final Order order, final LocalDateTime now) {
        final List<OrderSeat> orderSeats = orderSeatFinder.getOrderSeatsByOrderId(order.getId());
        final List<HoldHistory> holdHistories = holdHistoryFinder.findByHoldToken(order.getHoldToken());
        final List<Long> seatIds = orderSeats.stream().map(OrderSeat::getSeatId).toList();

        orderLifecycleDomainService.expire(order, orderSeats, holdHistories, now);
        applicationEventPublisher.publishEvent(new OrderExpiredEvent(order.getPerformanceId(), order.getHoldToken(), seatIds));
    }
}
