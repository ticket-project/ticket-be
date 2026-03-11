package com.ticket.core.domain.order.query.usecase;

import com.ticket.core.api.controller.response.OrderDetailResponse;
import com.ticket.core.domain.member.Member;
import com.ticket.core.domain.member.MemberFinder;
import com.ticket.core.domain.order.application.OrderExpirationApplicationService;
import com.ticket.core.domain.order.finder.OrderFinder;
import com.ticket.core.domain.order.finder.OrderSeatFinder;
import com.ticket.core.domain.order.model.Order;
import com.ticket.core.domain.order.model.OrderSeat;
import com.ticket.core.domain.order.query.OrderDetailResponseMapper;
import com.ticket.core.domain.performanceseat.finder.PerformanceSeatFinder;
import com.ticket.core.domain.performanceseat.model.PerformanceSeat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetOrderDetailUseCase {

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
