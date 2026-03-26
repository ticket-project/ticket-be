package com.ticket.core.domain.order.query;

import com.ticket.core.domain.member.model.Member;
import com.ticket.core.domain.order.model.Order;
import com.ticket.core.domain.order.model.OrderSeat;
import com.ticket.core.domain.performance.model.Performance;
import com.ticket.core.domain.performanceseat.model.PerformanceSeat;
import com.ticket.core.domain.seat.model.Seat;
import com.ticket.core.domain.show.model.Show;
import com.ticket.core.domain.show.venue.Venue;

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

    public static GetOrderDetailUseCase.Output toResponse(
            final Order order,
            final List<OrderSeat> orderSeats,
            final List<PerformanceSeat> performanceSeats,
            final Member member,
            final LocalDateTime now
    ) {
        if (performanceSeats.isEmpty()) {
            throw new IllegalArgumentException("performanceSeats 는 비어 있을 수 없습니다.");
        }

        final Map<Long, PerformanceSeat> performanceSeatMap = performanceSeats.stream()
                .collect(Collectors.toMap(PerformanceSeat::getId, Function.identity()));

        final List<GetOrderDetailUseCase.TicketSeat> seats = orderSeats.stream()
                .map(orderSeat -> toTicketSeat(orderSeat, performanceSeatMap.get(orderSeat.getPerformanceSeatId())))
                .toList();

        final Performance performance = performanceSeats.getFirst().getPerformance();
        final Show show = performance.getShow();
        final Venue venue = show.getVenue();
        final BigDecimal ticketAmount = orderSeats.stream()
                .map(OrderSeat::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        final long remainingSeconds = order.isPending()
                ? Math.max(0L, Duration.between(now, order.getExpiresAt()).getSeconds())
                : 0L;

        return new GetOrderDetailUseCase.Output(
                order.getOrderKey(),
                order.getStatus(),
                order.getExpiresAt(),
                remainingSeconds,
                new GetOrderDetailUseCase.ShowInfo(show.getId(), show.getTitle(), show.getImage()),
                new GetOrderDetailUseCase.PerformanceInfo(
                        performance.getId(),
                        performance.getPerformanceNo(),
                        performance.getStartTime(),
                        venue == null ? null : venue.getName()
                ),
                new GetOrderDetailUseCase.BookerInfo(
                        member.getId(),
                        member.getName(),
                        member.getEmail() == null ? null : member.getEmail().getEmail()
                ),
                new GetOrderDetailUseCase.PriceInfo(
                        ticketAmount,
                        ZERO,
                        ZERO,
                        ZERO,
                        ticketAmount
                ),
                new GetOrderDetailUseCase.TicketInfo(orderSeats.size(), seats)
        );
    }

    private static GetOrderDetailUseCase.TicketSeat toTicketSeat(
            final OrderSeat orderSeat,
            final PerformanceSeat performanceSeat
    ) {
        final Seat seat = performanceSeat.getSeat();
        final String label = seat.getFloor() + "F " + seat.getSection() + "구역 " + seat.getRowNo() + "열 " + seat.getSeatNo() + "번";

        return new GetOrderDetailUseCase.TicketSeat(
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
