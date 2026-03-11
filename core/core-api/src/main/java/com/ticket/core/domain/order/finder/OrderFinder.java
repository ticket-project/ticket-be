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

    public Order findById(final Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA, "주문을 찾을 수 없습니다. id=" + orderId));
    }

    public Order findOwnedById(final Long orderId, final Long memberId) {
        return orderRepository.findByIdAndMemberId(orderId, memberId)
                .orElseThrow(() -> new CoreException(ErrorType.ORDER_NOT_OWNED));
    }

    public Order findPendingOwnedById(final Long orderId, final Long memberId) {
        final Order order = findOwnedById(orderId, memberId);
        if (order.getStatus() != OrderState.PENDING) {
            throw new CoreException(ErrorType.ORDER_NOT_PENDING);
        }
        return order;
    }
}
