package com.ticket.core.domain.order.shared;

import com.ticket.core.domain.order.model.Order;
import com.ticket.core.domain.order.model.OrderSeat;
import com.ticket.core.domain.order.repository.OrderSeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderTerminationContextLoader {

    private final OrderSeatRepository orderSeatRepository;

    public OrderTerminationContext load(final Order order) {
        final List<OrderSeat> orderSeats = orderSeatRepository.findAllByOrder_IdOrderByIdAsc(order.getId());
        final List<Long> seatIds = orderSeats.stream()
                .map(OrderSeat::getSeatId)
                .toList();
        return new OrderTerminationContext(orderSeats, seatIds);
    }
}
