package com.ticket.core.domain.order.expire;

import com.ticket.core.domain.order.event.OrderExpiredEvent;
import com.ticket.core.domain.order.model.Order;
import com.ticket.core.domain.order.repository.OrderRepository;
import com.ticket.core.domain.order.shared.OrderTerminationContext;
import com.ticket.core.domain.order.shared.OrderTerminationContextLoader;
import com.ticket.core.domain.order.shared.OrderTerminationResult;
import com.ticket.core.enums.OrderState;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ExpireOrderUseCase {

    private final OrderRepository orderRepository;
    private final OrderTerminationContextLoader contextLoader;
    private final OrderExpirer orderExpirer;
    private final ApplicationEventPublisher applicationEventPublisher;

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
        final OrderTerminationContext context = contextLoader.load(order);
        orderExpirer.expire(order, context, now)
                .ifPresent(this::publishExpiredEvent);
    }

    private void publishExpiredEvent(final OrderTerminationResult result) {
        applicationEventPublisher.publishEvent(new OrderExpiredEvent(result.performanceId(), result.holdKey(), result.seatIds()));
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
