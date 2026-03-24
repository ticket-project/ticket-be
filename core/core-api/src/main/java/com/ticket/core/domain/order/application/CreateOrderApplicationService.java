package com.ticket.core.domain.order.application;

import com.ticket.core.domain.order.model.Order;
import com.ticket.core.domain.order.model.OrderSeat;
import com.ticket.core.domain.order.repository.OrderRepository;
import com.ticket.core.domain.order.repository.OrderSeatRepository;
import com.ticket.core.domain.performanceseat.model.PerformanceSeat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CreateOrderApplicationService {

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

        final Order order = orderRepository.save(new Order(memberId, performanceId, orderKey, holdKey, totalAmount, expiresAt));

        final List<OrderSeat> orderSeats = performanceSeats.stream()
                .map(seat -> new OrderSeat(order, seat.getId(), seat.getSeat().getId(), seat.getPrice()))
                .toList();
        orderSeatRepository.saveAll(orderSeats);
        return order;
    }

}
