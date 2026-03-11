package com.ticket.core.domain.order.query;

import com.ticket.core.api.controller.response.OrderDetailResponse;
import com.ticket.core.domain.member.Member;
import com.ticket.core.domain.order.model.Order;
import com.ticket.core.domain.order.model.OrderSeat;
import com.ticket.core.domain.performance.Performance;
import com.ticket.core.domain.performanceseat.model.PerformanceSeat;
import com.ticket.core.domain.seat.Seat;
import com.ticket.core.domain.show.Show;
import com.ticket.core.domain.show.Venue;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class OrderDetailResponseMapper {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private OrderDetailResponseMapper() {
    }

    public static OrderDetailResponse toResponse(
            final Order order,
            final List<OrderSeat> orderSeats,
            final List<PerformanceSeat> performanceSeats,
            final Member member
    ) {
        if (performanceSeats.isEmpty()) {
            throw new IllegalArgumentException("performanceSeats는 비어 있을 수 없습니다.");
        }

        final Map<Long, PerformanceSeat> performanceSeatMap = performanceSeats.stream()
                .collect(Collectors.toMap(PerformanceSeat::getId, Function.identity()));

        final List<OrderDetailResponse.TicketSeat> seats = orderSeats.stream()
                .map(orderSeat -> toTicketSeat(orderSeat, performanceSeatMap.get(orderSeat.getPerformanceSeatId())))
                .toList();

        final Performance performance = performanceSeats.getFirst().getPerformance();
        final Show show = performance.getShow();
        final Venue venue = show.getVenue();
        final BigDecimal ticketAmount = orderSeats.stream()
                .map(OrderSeat::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        final long remainingSeconds = order.isPending()
                ? Math.max(0L, Duration.between(LocalDateTime.now(), order.getExpiresAt()).getSeconds())
                : 0L;

        return new OrderDetailResponse(
                order.getId(),
                order.getStatus(),
                order.getExpiresAt(),
                remainingSeconds,
                new OrderDetailResponse.ShowInfo(show.getId(), show.getTitle(), show.getImage()),
                new OrderDetailResponse.PerformanceInfo(
                        performance.getId(),
                        performance.getPerformanceNo(),
                        performance.getStartTime(),
                        venue == null ? null : venue.getName()
                ),
                new OrderDetailResponse.BookerInfo(
                        member.getId(),
                        member.getName(),
                        member.getEmail() == null ? null : member.getEmail().getEmail()
                ),
                new OrderDetailResponse.PriceInfo(
                        ticketAmount,
                        ZERO,
                        ZERO,
                        ZERO,
                        ticketAmount
                ),
                new OrderDetailResponse.TicketInfo(orderSeats.size(), seats)
        );
    }

    private static OrderDetailResponse.TicketSeat toTicketSeat(
            final OrderSeat orderSeat,
            final PerformanceSeat performanceSeat
    ) {
        final Seat seat = performanceSeat.getSeat();
        final String label = seat.getFloor() + "F " + seat.getSection() + "구역 " + seat.getRowNo() + "열 " + seat.getSeatNo() + "번";

        return new OrderDetailResponse.TicketSeat(
                orderSeat.getPerformanceSeatId(),
                orderSeat.getSeatId(),
                seat.getFloor(),
                seat.getSection(),
                seat.getRowNo(),
                seat.getSeatNo(),
                label,
                orderSeat.getPrice()
        );
    }
}
