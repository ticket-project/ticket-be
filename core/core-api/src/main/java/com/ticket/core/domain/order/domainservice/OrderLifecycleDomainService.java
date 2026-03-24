package com.ticket.core.domain.order.domainservice;

import com.ticket.core.domain.order.model.Order;
import com.ticket.core.domain.order.model.OrderSeat;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@NoArgsConstructor
public class OrderLifecycleDomainService {

    public void cancel(
            final Order order,
            final List<OrderSeat> orderSeats,
            final LocalDateTime now
    ) {
        order.cancel(now);
        orderSeats.forEach(OrderSeat::cancel);
    }

    public void expire(
            final Order order,
            final List<OrderSeat> orderSeats,
            final LocalDateTime now
    ) {
        order.expire(now);
        orderSeats.forEach(OrderSeat::expire);
    }
}
