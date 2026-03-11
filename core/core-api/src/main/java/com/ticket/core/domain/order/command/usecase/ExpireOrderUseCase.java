package com.ticket.core.domain.order.command.usecase;

import com.ticket.core.domain.order.application.OrderExpirationApplicationService;
import com.ticket.core.domain.order.model.Order;
import com.ticket.core.domain.order.repository.OrderRepository;
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
