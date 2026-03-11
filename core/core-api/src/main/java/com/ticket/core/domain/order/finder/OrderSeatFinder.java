package com.ticket.core.domain.order.finder;

import com.ticket.core.domain.order.model.OrderSeat;
import com.ticket.core.domain.order.repository.OrderSeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderSeatFinder {
    private final OrderSeatRepository orderSeatRepository;

    public List<OrderSeat> getOrderSeatsByOrderId(final Long orderId) {
        return orderSeatRepository.findAllByOrder_IdOrderByIdAsc(orderId);
    }

}
