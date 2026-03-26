package com.ticket.core.domain.order.command.cancel;

import com.ticket.core.domain.member.query.MemberFinder;
import com.ticket.core.domain.order.model.Order;
import com.ticket.core.domain.order.model.OrderSeat;
import com.ticket.core.domain.order.command.release.HoldReleaseRequestedEvent;
import com.ticket.core.domain.order.command.release.HoldReleaseOutboxWriter;
import com.ticket.core.domain.order.repository.OrderRepository;
import com.ticket.core.domain.order.repository.OrderSeatRepository;
import com.ticket.core.domain.order.OrderTerminationResult;
import com.ticket.core.domain.order.model.OrderState;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CancelOrderUseCase {

    private final MemberFinder memberFinder;
    private final OrderRepository orderRepository;
    private final OrderSeatRepository orderSeatRepository;
    private final OrderCanceler orderCanceler;
    private final HoldReleaseOutboxWriter holdReleaseOutboxWriter;
    private final ApplicationEventPublisher applicationEventPublisher;

    public record Input(String orderKey, Long memberId) {}

    @Transactional
    public void execute(final Input input) {
        memberFinder.findActiveMemberById(input.memberId());
        final Order order = getPendingOwnedOrder(input.orderKey(), input.memberId());
        final List<OrderSeat> orderSeats = orderSeatRepository.findAllByOrder_IdOrderByIdAsc(order.getId());
        final OrderTerminationResult result = orderCanceler.cancel(order, orderSeats, LocalDateTime.now());
        final Long outboxId = holdReleaseOutboxWriter.append(result);
        applicationEventPublisher.publishEvent(new HoldReleaseRequestedEvent(outboxId));
    }

    private Order getPendingOwnedOrder(final String orderKey, final Long memberId) {
        final Order order = orderRepository.findByOrderKeyAndMemberIdForUpdate(orderKey, memberId)
                .orElseThrow(() -> new CoreException(ErrorType.ORDER_NOT_OWNED));
        if (order.getStatus() != OrderState.PENDING) {
            throw new CoreException(ErrorType.ORDER_NOT_PENDING);
        }
        return order;
    }
}
