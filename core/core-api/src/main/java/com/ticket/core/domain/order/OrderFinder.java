package com.ticket.core.domain.order;

import com.ticket.core.enums.OrderState;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderFinder {

    private final OrderRepository orderRepository;

    public Order findById(final Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA, "주문을 찾을 수 없습니다. id=" + orderId));
    }

    public Order findByOrderNo(final String orderNo) {
        return orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA, "주문을 찾을 수 없습니다. orderNo=" + orderNo));
    }

    public List<Order> findPendingOrdersByPerformanceId(final Long performanceId) {
        return orderRepository.findByPerformanceIdAndStateIn(performanceId, List.of(OrderState.PENDING));
    }
}
