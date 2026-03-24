package com.ticket.core.domain.order.cancel;

import com.ticket.core.domain.member.MemberFinder;
import com.ticket.core.domain.order.event.OrderCancelledEvent;
import com.ticket.core.domain.order.model.Order;
import com.ticket.core.domain.order.repository.OrderRepository;
import com.ticket.core.domain.order.shared.OrderTerminationContext;
import com.ticket.core.domain.order.shared.OrderTerminationContextLoader;
import com.ticket.core.domain.order.shared.OrderTerminationResult;
import com.ticket.core.enums.OrderState;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CancelOrderUseCase {

    private final MemberFinder memberFinder;
    private final OrderRepository orderRepository;
    //밑에 이거 뭔데??? 가독성 떨어지는거 다 없애자.
    private final OrderTerminationContextLoader contextLoader;
    private final OrderCanceler orderCanceler;
    private final ApplicationEventPublisher applicationEventPublisher;

    public record Input(String orderKey, Long memberId) {}

    @Transactional
    public void execute(final Input input) {
        memberFinder.findActiveMemberById(input.memberId());
        final Order order = getPendingOwnedOrder(input);
        final OrderTerminationContext context = contextLoader.load(order);
        final OrderTerminationResult result = orderCanceler.cancel(order, context, LocalDateTime.now());
        applicationEventPublisher.publishEvent(new OrderCancelledEvent(result.performanceId(), result.holdKey(), result.seatIds()));
    }

    private Order getPendingOwnedOrder(final Input input) {
        final Order order = orderRepository.findByOrderKeyAndMemberIdForUpdate(input.orderKey(), input.memberId())
                .orElseThrow(() -> new CoreException(ErrorType.ORDER_NOT_OWNED));
        if (order.getStatus() != OrderState.PENDING) {
            throw new CoreException(ErrorType.ORDER_NOT_PENDING);
        }
        return order;
    }
}
