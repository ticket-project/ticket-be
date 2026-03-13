package com.ticket.core.domain.order.application;

import com.ticket.core.domain.order.model.Order;
import com.ticket.core.domain.order.model.OrderSeat;
import com.ticket.core.domain.order.repository.OrderRepository;
import com.ticket.core.domain.order.repository.OrderSeatRepository;
import com.ticket.core.domain.performanceseat.model.PerformanceSeat;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CreateOrderApplicationService {

    private static final String PENDING_ORDER_CONSTRAINT_NAME = "UK_ORDERS_PENDING_MEMBER_PERF";

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
        final BigDecimal totalAmount = performanceSeats.stream()
                .map(PerformanceSeat::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        final String orderKey = orderKeyGenerator.generate();

        final Order order;
        try {
            order = orderRepository.save(
                    new Order(memberId, performanceId, orderKey, holdKey, totalAmount, expiresAt)
            );
        } catch (final DataIntegrityViolationException e) {
            if (isPendingOrderConstraintViolation(e)) {
                throw new CoreException(ErrorType.PENDING_ORDER_ALREADY_EXISTS);
            }
            throw e;
        }

        final List<OrderSeat> orderSeats = performanceSeats.stream()
                .map(seat -> new OrderSeat(order, seat.getId(), seat.getSeat().getId(), seat.getPrice()))
                .toList();
        orderSeatRepository.saveAll(orderSeats);

        return order;
    }

    private boolean isPendingOrderConstraintViolation(final DataIntegrityViolationException e) {
        Throwable cause = e;
        while (cause != null) {
            final String message = cause.getMessage();
            if (message != null && message.contains(PENDING_ORDER_CONSTRAINT_NAME)) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }
}
