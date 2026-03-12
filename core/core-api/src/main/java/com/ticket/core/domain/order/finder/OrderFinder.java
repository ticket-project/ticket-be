package com.ticket.core.domain.order.finder;

import com.ticket.core.domain.order.model.Order;
import com.ticket.core.domain.order.repository.OrderRepository;
import com.ticket.core.enums.OrderState;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderFinder {

    private final OrderRepository orderRepository;

    public Order findOwnedByOrderKey(final String orderKey, final Long memberId) {
        return orderRepository.findByOrderKeyAndMemberId(orderKey, memberId)
                .orElseThrow(() -> new CoreException(ErrorType.ORDER_NOT_OWNED));
    }

    public Order findPendingOwnedByOrderKey(final String orderKey, final Long memberId) {
        final Order order = findOwnedByOrderKey(orderKey, memberId);
        if (order.getStatus() != OrderState.PENDING) {
            throw new CoreException(ErrorType.ORDER_NOT_PENDING);
        }
        return order;
    }
}
