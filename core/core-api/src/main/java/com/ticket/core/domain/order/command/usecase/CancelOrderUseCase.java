package com.ticket.core.domain.order.command.usecase;

import com.ticket.core.domain.order.application.OrderLifecycleApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CancelOrderUseCase {

    private final OrderLifecycleApplicationService orderLifecycleApplicationService;

    public record Input(String orderKey, Long memberId) {}
    public record Output() {}

    @Transactional
    public Output execute(final Input input) {
        orderLifecycleApplicationService.cancelPendingOrder(input.orderKey(), input.memberId(), LocalDateTime.now());
        return new Output();
    }
}
