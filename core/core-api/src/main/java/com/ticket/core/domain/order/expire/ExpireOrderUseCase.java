package com.ticket.core.domain.order.expire;

import com.ticket.core.domain.order.model.Order;
import com.ticket.core.domain.order.model.OrderSeat;
import com.ticket.core.domain.order.release.HoldReleaseOutboxWriter;
import com.ticket.core.domain.order.repository.OrderRepository;
import com.ticket.core.domain.order.repository.OrderSeatRepository;
import com.ticket.core.domain.order.shared.OrderTerminationResult;
import com.ticket.core.enums.OrderState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpireOrderUseCase {

    private final OrderRepository orderRepository;
    private final OrderSeatRepository orderSeatRepository;
    private final OrderExpirer orderExpirer;
    private final HoldReleaseOutboxWriter holdReleaseOutboxWriter;

    @Transactional
    public void expireByOrderId(final Long orderId, final LocalDateTime now) {
        expire(findPendingOrder(orderId), now);
    }

    @Transactional
    public void expireByHoldKey(final String holdKey, final LocalDateTime now) {
        expire(findPendingOrder(holdKey), now);
    }

    private void expire(final Order order, final LocalDateTime now) {
        if (order == null) {
            return;
        }
        final List<OrderSeat> orderSeats = orderSeatRepository.findAllByOrder_IdOrderByIdAsc(order.getId());
        final OrderTerminationResult result = orderExpirer.expire(order, orderSeats, now);
        holdReleaseOutboxWriter.append(result);
    }

    private Order findPendingOrder(final Long orderId) {
        return orderRepository.findByIdAndStatusForUpdate(orderId, OrderState.PENDING)
                .orElse(null);
    }

    private Order findPendingOrder(final String holdKey) {
        return orderRepository.findByHoldKeyAndStatusForUpdate(holdKey, OrderState.PENDING)
                .orElse(null);
    }
}
