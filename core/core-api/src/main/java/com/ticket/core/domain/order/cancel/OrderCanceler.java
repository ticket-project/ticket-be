package com.ticket.core.domain.order.cancel;

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
public class OrderCanceler {

    private final HoldHistoryRecorder holdHistoryRecorder;

    public OrderTerminationResult cancel(
            final Order order,
            final List<OrderSeat> orderSeats,
            final LocalDateTime now
    ) {
        order.cancel(now);
        orderSeats.forEach(OrderSeat::cancel);
        holdHistoryRecorder.recordCanceled(
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
