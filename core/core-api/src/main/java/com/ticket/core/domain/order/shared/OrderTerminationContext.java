package com.ticket.core.domain.order.shared;

import com.ticket.core.domain.order.model.OrderSeat;

import java.util.List;

public record OrderTerminationContext(
        List<OrderSeat> orderSeats,
        List<Long> seatIds
) {
}
