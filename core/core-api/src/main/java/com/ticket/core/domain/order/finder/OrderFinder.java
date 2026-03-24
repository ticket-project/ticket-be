package com.ticket.core.domain.order.finder;

import com.ticket.core.domain.order.model.Order;
import com.ticket.core.domain.order.repository.OrderRepository;
import com.ticket.core.enums.OrderState;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderFinder {

    private final OrderRepository orderRepository;

    public Order findOwnedByOrderKey(final String orderKey, final Long memberId) {
        return orderRepository.findByOrderKeyAndMemberId(orderKey, memberId)
                .orElseThrow(() -> new CoreException(ErrorType.ORDER_NOT_OWNED));
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public Order findPendingOwnedByOrderKeyForUpdate(final String orderKey, final Long memberId) {
        final Order order = orderRepository.findByOrderKeyAndMemberIdForUpdate(orderKey, memberId)
                .orElseThrow(() -> new CoreException(ErrorType.ORDER_NOT_OWNED));
        if (order.getStatus() != OrderState.PENDING) {
            throw new CoreException(ErrorType.ORDER_NOT_PENDING);
        }
        return order;
    }

    public boolean findByMemberIdAndPerformanceIdAndStatus(final Long memberId, final Long performanceId, final OrderState status) {
        return orderRepository.findByMemberIdAndPerformanceIdAndStatus(memberId, performanceId, status).isPresent();
    }

    public Order findPendingOrder(final Long orderId) {
        return orderRepository.findByIdAndStatusForUpdate(orderId, OrderState.PENDING)
                .orElse(null);
    }

    public Order findPendingOrderByHoldKey(final String holdKey) {
        return orderRepository.findByHoldKeyAndStatusForUpdate(holdKey, OrderState.PENDING)
                .orElse(null);
    }

    public Slice<Order> findAllByStatusAndExpiresAtBefore(final OrderState orderState, final LocalDateTime now, final PageRequest pageRequest) {
        return orderRepository.findAllByStatusAndExpiresAtBefore(orderState, now, pageRequest);
    }
}
