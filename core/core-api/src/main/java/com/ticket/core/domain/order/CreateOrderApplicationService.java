package com.ticket.core.domain.order;

import com.ticket.core.domain.hold.HoldHistory;
import com.ticket.core.domain.hold.HoldHistoryRepository;
import com.ticket.core.domain.performanceseat.PerformanceSeat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CreateOrderApplicationService {

    private final OrderRepository orderRepository;
    private final OrderSeatRepository orderSeatRepository;
    private final HoldHistoryRepository holdHistoryRepository;

    public Long createPendingOrder(
            final Long memberId,
            final Long performanceId,
            final String holdToken,
            final LocalDateTime expiresAt,
            final List<PerformanceSeat> performanceSeats
    ) {
        final BigDecimal totalAmount = performanceSeats.stream()
                .map(PerformanceSeat::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        final Order order = orderRepository.save(
                new Order(memberId, performanceId, holdToken, totalAmount, expiresAt)
        );

        final List<OrderSeat> orderSeats = performanceSeats.stream()
                .map(seat -> new OrderSeat(order, seat.getId(), seat.getSeat().getId(), seat.getPrice()))
                .toList();
        orderSeatRepository.saveAll(orderSeats);

        final List<HoldHistory> holdHistories = performanceSeats.stream()
                .map(seat -> new HoldHistory(
                        holdToken,
                        memberId,
                        performanceId,
                        seat.getId(),
                        seat.getSeat().getId(),
                        expiresAt
                ))
                .toList();
        holdHistoryRepository.saveAll(holdHistories);

        return order.getId();
    }
}
