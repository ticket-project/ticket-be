package com.ticket.core.domain.order.usecase;

import com.ticket.core.api.controller.response.OrderDetailResponse;
import com.ticket.core.domain.member.Member;
import com.ticket.core.domain.member.MemberFinder;
import com.ticket.core.domain.order.OrderExpirationApplicationService;
import com.ticket.core.domain.order.Order;
import com.ticket.core.domain.order.OrderDetailResponseMapper;
import com.ticket.core.domain.order.OrderFinder;
import com.ticket.core.domain.order.OrderSeat;
import com.ticket.core.domain.order.OrderSeatFinder;
import com.ticket.core.domain.performanceseat.PerformanceSeat;
import com.ticket.core.domain.performanceseat.PerformanceSeatFinder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetOrderUseCase {

    private final OrderFinder orderFinder;
    private final OrderSeatFinder orderSeatFinder;
    private final PerformanceSeatFinder performanceSeatFinder;
    private final MemberFinder memberFinder;
    private final OrderExpirationApplicationService orderExpirationApplicationService;

    public record Input(Long orderId, Long memberId) {}
    public record Output(OrderDetailResponse order) {}

    @Transactional
    public Output execute(final Input input) {
        final Order order = orderFinder.findOwnedById(input.orderId(), input.memberId());
        final LocalDateTime now = LocalDateTime.now();
        if (order.isPending() && order.isExpired(now)) {
            orderExpirationApplicationService.expire(order, now);
        }

        final List<OrderSeat> orderSeats = orderSeatFinder.getOrderSeatsByOrderId(order.getId());
        final List<PerformanceSeat> performanceSeats = performanceSeatFinder.findAllByOrderSeats(orderSeats);
        final Member member = memberFinder.findActiveMemberById(input.memberId());

        return new Output(OrderDetailResponseMapper.toResponse(order, orderSeats, performanceSeats, member));
    }
}
