package com.ticket.core.domain.order.expire;

import com.ticket.core.domain.hold.application.HoldHistoryRecorder;
import com.ticket.core.domain.order.model.Order;
import com.ticket.core.domain.order.model.OrderSeat;
import com.ticket.core.domain.order.shared.OrderTerminationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderExpirer {

    private final HoldHistoryRecorder holdHistoryRecorder;

    public OrderTerminationResult expire(
            final Order order,
            final List<OrderSeat> orderSeats,
            final LocalDateTime now
    ) {
        order.expire(now);
        holdHistoryRecorder.recordExpired(
                order.getMemberId(),
                order.getPerformanceId(),
                order.getHoldKey(),
                now,
                orderSeats
        );
        return new OrderTerminationResult(order.getPerformanceId(), order.getHoldKey(), extractSeatIds(orderSeats));
    }

    private List<Long> extractSeatIds(final List<OrderSeat> orderSeats) {
        return orderSeats.stream()
                .map(OrderSeat::getSeatId)
                .toList();
    }
}
