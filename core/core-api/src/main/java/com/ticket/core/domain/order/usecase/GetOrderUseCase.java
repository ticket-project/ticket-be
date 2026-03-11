package com.ticket.core.domain.order.usecase;

import com.ticket.core.api.controller.response.OrderDetailResponse;
import com.ticket.core.domain.order.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetOrderUseCase {

    private final OrderService orderService;

    public record Input(Long orderId, Long memberId) {}
    public record Output(OrderDetailResponse order) {}

    public Output execute(final Input input) {
        return new Output(orderService.getOrderDetail(input.orderId(), input.memberId()));
    }
}
