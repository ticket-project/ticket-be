package com.ticket.core.domain.order;

import com.ticket.core.domain.hold.HoldHistory;
import com.ticket.core.domain.hold.HoldHistoryFinder;
import com.ticket.core.domain.hold.HoldReleaseApplicationService;
import com.ticket.core.domain.performanceseat.SeatStatusPublishApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderExpirationApplicationService {

    private final OrderSeatFinder orderSeatFinder;
    private final HoldHistoryFinder holdHistoryFinder;
    private final OrderLifecycleDomainService orderLifecycleDomainService;
    private final HoldReleaseApplicationService holdReleaseApplicationService;
    private final SeatStatusPublishApplicationService seatStatusPublishApplicationService;

    public void expire(final Order order, final LocalDateTime now) {
        final List<OrderSeat> orderSeats = orderSeatFinder.getOrderSeatsByOrderId(order.getId());
        final List<HoldHistory> holdHistories = holdHistoryFinder.findByHoldToken(order.getHoldToken());

        orderLifecycleDomainService.expire(order, orderSeats, holdHistories, now);
        holdReleaseApplicationService.releaseBySeatIds(
                order.getPerformanceId(),
                order.getHoldToken(),
                orderSeats.stream().map(OrderSeat::getSeatId).toList()
        );
        seatStatusPublishApplicationService.publishReleased(order.getPerformanceId(), orderSeats);
    }
}
