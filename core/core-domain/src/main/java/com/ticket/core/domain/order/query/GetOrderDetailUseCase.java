package com.ticket.core.domain.order.query;

import com.ticket.core.domain.member.model.Member;
import com.ticket.core.domain.member.query.MemberFinder;
import com.ticket.core.domain.order.model.Order;
import com.ticket.core.domain.order.model.OrderSeat;
import com.ticket.core.domain.order.model.OrderState;
import com.ticket.core.domain.performanceseat.model.PerformanceSeat;
import com.ticket.core.domain.performanceseat.query.PerformanceSeatFinder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetOrderDetailUseCase {

    private final OrderFinder orderFinder;
    private final OrderSeatFinder orderSeatFinder;
    private final PerformanceSeatFinder performanceSeatFinder;
    private final MemberFinder memberFinder;
    private final Clock clock;

    public record Input(String orderKey, Long memberId) {}
    public record Output(
            String orderKey,
            OrderState status,
            LocalDateTime expiresAt,
            long remainingSeconds,
            ShowInfo show,
            PerformanceInfo performance,
            BookerInfo booker,
            PriceInfo price,
            TicketInfo tickets
    ) {}

    public record ShowInfo(Long showId, String title, String imageUrl) {}

    public record PerformanceInfo(Long performanceId, Long performanceNo, LocalDateTime startTime, String venueName) {}

    public record BookerInfo(Long memberId, String name, String email) {}

    public record PriceInfo(
            BigDecimal ticketAmount,
            BigDecimal bookingFee,
            BigDecimal deliveryFee,
            BigDecimal discountAmount,
            BigDecimal totalAmount
    ) {}

    public record TicketInfo(int count, List<TicketSeat> seats) {}

    public record TicketSeat(
            Long performanceSeatId,
            Long seatId,
            int floor,
            String section,
            String rowNo,
            String seatNo,
            String label,
            BigDecimal price
    ) {}

    public Output execute(final Input input) {
        final Order order = orderFinder.findOwnedByOrderKey(input.orderKey(), input.memberId());
        final List<OrderSeat> orderSeats = orderSeatFinder.getOrderSeatsByOrderId(order.getId());
        final List<PerformanceSeat> performanceSeats = performanceSeatFinder.findAllByOrderSeats(orderSeats);
        final Member member = memberFinder.findActiveMemberById(input.memberId());
        final LocalDateTime now = LocalDateTime.now(clock);

        return OrderDetailResponseMapper.toResponse(order, orderSeats, performanceSeats, member, now);
    }
}
