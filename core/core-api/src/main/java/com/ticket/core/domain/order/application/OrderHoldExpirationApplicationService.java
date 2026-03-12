package com.ticket.core.domain.order.application;

import com.ticket.core.domain.order.model.Order;
import com.ticket.core.domain.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OrderHoldExpirationApplicationService {

    private final OrderRepository orderRepository;
    private final OrderExpirationApplicationService orderExpirationApplicationService;

    @Transactional
    public void expireByHoldToken(final String holdToken, final LocalDateTime now) {
        final Order order = orderRepository.findByHoldToken(holdToken)
                .orElse(null);
        if (order == null || !order.isPending()) {
            return;
        }

        orderExpirationApplicationService.expire(order, now);
    }
}
