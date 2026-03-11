package com.ticket.core.domain.order;

import com.ticket.core.domain.order.usecase.ExpireOrderUseCase;
import com.ticket.core.enums.OrderState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderExpirationScheduler {

    private final OrderRepository orderRepository;
    private final ExpireOrderUseCase expireOrderUseCase;

    @Scheduled(fixedDelayString = "300000")
    public void expirePendingOrders() {
        final List<Order> expiredOrders = orderRepository.findAllByStatusAndExpiresAtBefore(
                OrderState.PENDING,
                LocalDateTime.now()
        );

        for (final Order order : expiredOrders) {
            try {
                expireOrderUseCase.execute(new ExpireOrderUseCase.Input(order.getId(), LocalDateTime.now()));
            } catch (final RuntimeException e) {
                log.error("주문 만료 처리 실패: orderId={}", order.getId(), e);
            }
        }
    }
}
