package com.ticket.core.domain.order.command;

import com.ticket.core.domain.order.command.usecase.ExpireOrderUseCase;
import com.ticket.core.domain.order.model.Order;
import com.ticket.core.domain.order.repository.OrderRepository;
import com.ticket.core.enums.OrderState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderExpirationScheduler {

    private static final int BATCH_SIZE = 100;

    private final OrderRepository orderRepository;
    private final ExpireOrderUseCase expireOrderUseCase;

    @Scheduled(fixedDelayString = "300000")
    public void expirePendingOrders() {
        final LocalDateTime now = LocalDateTime.now();
        Slice<Order> expiredOrders;
        do {
            expiredOrders = orderRepository.findAllByStatusAndExpiresAtBefore(
                    OrderState.PENDING,
                    now,
                    PageRequest.of(0, BATCH_SIZE)
            );

            for (final Order order : expiredOrders.getContent()) {
                try {
                    expireOrderUseCase.execute(new ExpireOrderUseCase.Input(order.getId(), now));
                } catch (final RuntimeException e) {
                    log.error("주문 만료 처리 실패: orderId={}", order.getId(), e);
                }
            }
        } while (expiredOrders.hasContent());
    }
}
