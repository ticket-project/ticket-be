package com.ticket.core.domain.order.cancel;

import com.ticket.core.domain.hold.application.HoldHistoryRecorder;
import com.ticket.core.domain.order.model.Order;
import com.ticket.core.domain.order.model.OrderSeat;
import com.ticket.core.domain.order.shared.OrderTerminationContext;
import com.ticket.core.domain.order.shared.OrderTerminationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class OrderCanceler {

    private final HoldHistoryRecorder holdHistoryRecorder;

    public OrderTerminationResult cancel(
            final Order order,
            final OrderTerminationContext context,
            final LocalDateTime now
    ) {
        order.cancel(now);
        context.orderSeats().forEach(OrderSeat::cancel);
        holdHistoryRecorder.recordCanceled(
                order.getMemberId(),
                order.getPerformanceId(),
                order.getHoldKey(),
                now,
                context.orderSeats()
        );
        return new OrderTerminationResult(order.getPerformanceId(), order.getHoldKey(), context.seatIds());
    }
}
