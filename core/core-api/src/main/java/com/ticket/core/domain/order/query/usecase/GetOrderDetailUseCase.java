package com.ticket.core.domain.order.query.usecase;

import com.ticket.core.api.controller.response.OrderDetailResponse;
import com.ticket.core.domain.member.Member;
import com.ticket.core.domain.member.MemberFinder;
import com.ticket.core.domain.order.finder.OrderFinder;
import com.ticket.core.domain.order.finder.OrderSeatFinder;
import com.ticket.core.domain.order.model.Order;
import com.ticket.core.domain.order.model.OrderSeat;
import com.ticket.core.domain.order.query.OrderDetailResponseMapper;
import com.ticket.core.domain.performanceseat.finder.PerformanceSeatFinder;
import com.ticket.core.domain.performanceseat.model.PerformanceSeat;
import com.ticket.core.enums.OrderState;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
            @Schema(description = "주문 키", example = "ORD-3f24c6bc355148f6bf941f0b2f2a6c2b") String orderKey,
            @Schema(description = "주문 상태", example = "PENDING") OrderState status,
            @Schema(description = "주문 만료 시각") LocalDateTime expiresAt,
            @Schema(description = "현재 기준 남은 선점 시간(초)", example = "287") long remainingSeconds,
            @Schema(description = "공연 정보") OrderDetailResponse.ShowInfo show,
            @Schema(description = "회차 정보") OrderDetailResponse.PerformanceInfo performance,
            @Schema(description = "예매자 정보") OrderDetailResponse.BookerInfo booker,
            @Schema(description = "금액 정보") OrderDetailResponse.PriceInfo price,
            @Schema(description = "주문 좌석 정보") OrderDetailResponse.TicketInfo tickets
    ) {}

    public Output execute(final Input input) {
        final Order order = orderFinder.findOwnedByOrderKey(input.orderKey(), input.memberId());
        final List<OrderSeat> orderSeats = orderSeatFinder.getOrderSeatsByOrderId(order.getId());
        final List<PerformanceSeat> performanceSeats = performanceSeatFinder.findAllByOrderSeats(orderSeats);
        final Member member = memberFinder.findActiveMemberById(input.memberId());

        final OrderDetailResponse result = OrderDetailResponseMapper.toResponse(order, orderSeats, performanceSeats, member, clock);
        return new Output(
                result.orderKey(), result.status(), result.expiresAt(), result.remainingSeconds(),
                result.show(), result.performance(), result.booker(), result.price(), result.tickets()
        );
    }
}
