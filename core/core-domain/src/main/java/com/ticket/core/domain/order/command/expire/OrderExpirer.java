package com.ticket.core.domain.order.command.expire;

import com.ticket.core.domain.hold.command.HoldHistoryRecorder;
import com.ticket.core.domain.order.model.Order;
import com.ticket.core.domain.order.model.OrderSeat;
import com.ticket.core.domain.order.OrderTerminationResult;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class OrderExpirer {

    private final HoldHistoryRecorder holdHistoryRecorder;

    public OrderTerminationResult expire(
            final Order order,
            final List<OrderSeat> orderSeats,
            final LocalDateTime now
    ) {
        validateOrderSeatsBelongTo(order, orderSeats);
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

    private void validateOrderSeatsBelongTo(final Order order, final List<OrderSeat> orderSeats) {
        final boolean hasForeignOrderSeat = orderSeats.stream()
                .anyMatch(orderSeat -> !Objects.equals(orderSeat.getOrder().getId(), order.getId()));
        if (hasForeignOrderSeat) {
            throw new CoreException(ErrorType.INVALID_REQUEST, "orderSeats는 같은 order에 속해야 합니다.");
        }
    }
}
