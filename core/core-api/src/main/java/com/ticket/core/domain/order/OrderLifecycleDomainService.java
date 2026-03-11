package com.ticket.core.domain.order;

import com.ticket.core.domain.hold.HoldHistory;
import com.ticket.core.enums.HoldReleaseReason;
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
            final List<HoldHistory> holdHistories,
            final LocalDateTime now
    ) {
        order.cancel(now);
        orderSeats.forEach(OrderSeat::cancel);
        holdHistories.forEach(history -> history.cancel(now, HoldReleaseReason.USER_CANCELED));
    }

    public void expire(
            final Order order,
            final List<OrderSeat> orderSeats,
            final List<HoldHistory> holdHistories,
            final LocalDateTime now
    ) {
        order.expire(now);
        orderSeats.forEach(OrderSeat::expire);
        holdHistories.forEach(history -> history.expire(now, HoldReleaseReason.TTL_EXPIRED));
    }
}
