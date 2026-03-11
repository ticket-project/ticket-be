package com.ticket.core.domain.order.usecase;

import com.ticket.core.domain.order.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CancelOrderUseCase {

    private final OrderService orderService;

    public record Input(Long orderId, Long memberId) {}

    public void execute(final Input input) {
        orderService.cancelOrder(input.orderId(), input.memberId());
    }
}
