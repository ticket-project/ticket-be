package com.ticket.core.domain.order.create;

import com.ticket.core.domain.order.model.Order;
import com.ticket.core.domain.order.model.OrderSeat;
import com.ticket.core.domain.order.repository.OrderRepository;
import com.ticket.core.domain.order.repository.OrderSeatRepository;
import com.ticket.core.domain.performanceseat.model.PerformanceSeat;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderCreator {

    private static final String PENDING_ORDER_UNIQUE_CONSTRAINT = "UK_ORDERS_PENDING_MEMBER_PERF";

    private final OrderRepository orderRepository;
    private final OrderSeatRepository orderSeatRepository;
    private final OrderKeyGenerator orderKeyGenerator;

    @Transactional
    public Order createPendingOrder(
            final Long memberId,
            final Long performanceId,
            final String holdKey,
            final LocalDateTime expiresAt,
            final List<PerformanceSeat> performanceSeats
    ) {
        try {
            return create(memberId, performanceId, holdKey, expiresAt, performanceSeats);
        } catch (final DataIntegrityViolationException e) {
            throw translateException(e, memberId, performanceId);
        }
    }

    private Order create(
            final Long memberId,
            final Long performanceId,
            final String holdKey,
            final LocalDateTime expiresAt,
            final List<PerformanceSeat> performanceSeats
    ) {
        final BigDecimal totalAmount = sumTotalAmount(performanceSeats);
        final String orderKey = orderKeyGenerator.generate();
        final Order order = orderRepository.save(new Order(memberId, performanceId, orderKey, holdKey, totalAmount, expiresAt));
        orderSeatRepository.saveAll(toOrderSeats(order, performanceSeats));
        return order;
    }

    private BigDecimal sumTotalAmount(final List<PerformanceSeat> performanceSeats) {
        return performanceSeats.stream()
                .map(PerformanceSeat::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<OrderSeat> toOrderSeats(final Order order, final List<PerformanceSeat> performanceSeats) {
        return performanceSeats.stream()
                .map(seat -> new OrderSeat(order, seat.getId(), seat.getSeat().getId(), seat.getPrice()))
                .toList();
    }

    private RuntimeException translateException(
            final DataIntegrityViolationException e,
            final Long memberId,
            final Long performanceId
    ) {
        if (!isPendingOrderDuplicate(e)) {
            return e;
        }
        log.warn("중복 PENDING 주문 생성 시도: memberId={}, performanceId={}", memberId, performanceId);
        return new CoreException(ErrorType.PENDING_ORDER_ALREADY_EXISTS);
    }

    private boolean isPendingOrderDuplicate(final DataIntegrityViolationException e) {
        if (!(e.getCause() instanceof ConstraintViolationException cause)) {
            return false;
        }
        return PENDING_ORDER_UNIQUE_CONSTRAINT.equals(cause.getConstraintName());
    }
}
