package com.ticket.core.domain.order.command.usecase;

import com.ticket.core.domain.hold.application.HoldReleaseApplicationService;
import com.ticket.core.domain.hold.finder.HoldHistoryFinder;
import com.ticket.core.domain.hold.model.HoldHistory;
import com.ticket.core.domain.order.domainservice.OrderLifecycleDomainService;
import com.ticket.core.domain.order.finder.OrderFinder;
import com.ticket.core.domain.order.finder.OrderSeatFinder;
import com.ticket.core.domain.order.model.Order;
import com.ticket.core.domain.order.model.OrderSeat;
import com.ticket.core.domain.performanceseat.application.SeatStatusPublishApplicationService;
import lombok.RequiredArgsConstructor;
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
    private final HoldReleaseApplicationService holdReleaseApplicationService;
    private final SeatStatusPublishApplicationService seatStatusPublishApplicationService;

    public record Input(Long orderId, Long memberId) {}
    public record Output() {}

    @Transactional
    public Output execute(final Input input) {
        final Order order = orderFinder.findPendingOwnedById(input.orderId(), input.memberId());
        final List<OrderSeat> orderSeats = orderSeatFinder.getOrderSeatsByOrderId(order.getId());
        final List<HoldHistory> holdHistories = holdHistoryFinder.findByHoldToken(order.getHoldToken());

        final LocalDateTime now = LocalDateTime.now();
        orderLifecycleDomainService.cancel(order, orderSeats, holdHistories, now);
        holdReleaseApplicationService.releaseByHoldToken(order.getHoldToken());
        seatStatusPublishApplicationService.publishReleased(order.getPerformanceId(), orderSeats);
        return new Output();
    }
}
