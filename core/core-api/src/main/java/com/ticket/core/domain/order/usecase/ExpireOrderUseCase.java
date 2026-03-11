package com.ticket.core.domain.order.usecase;

import com.ticket.core.domain.order.OrderExpirationApplicationService;
import com.ticket.core.domain.order.Order;
import com.ticket.core.domain.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ExpireOrderUseCase {

    private final OrderRepository orderRepository;
    private final OrderExpirationApplicationService orderExpirationApplicationService;

    public record Input(Long orderId, LocalDateTime now) {}
    public record Output() {}

    @Transactional
    public Output execute(final Input input) {
        final Order order = orderRepository.findById(input.orderId())
                .orElse(null);
        if (order == null || !order.isPending()) {
            return new Output();
        }

        orderExpirationApplicationService.expire(order, input.now());
        return new Output();
    }
}
