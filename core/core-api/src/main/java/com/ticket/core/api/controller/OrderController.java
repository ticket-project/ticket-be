package com.ticket.core.api.controller;

import com.ticket.core.api.controller.docs.OrderControllerDocs;
import com.ticket.core.domain.member.MemberPrincipal;
import com.ticket.core.domain.order.usecase.CancelOrderUseCase;
import com.ticket.core.domain.order.usecase.GetOrderUseCase;
import com.ticket.core.support.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController implements OrderControllerDocs {

    private final GetOrderUseCase getOrderUseCase;
    private final CancelOrderUseCase cancelOrderUseCase;

    @Override
    @GetMapping("/{orderId}")
    public ApiResponse<GetOrderUseCase.Output> getOrder(
            @PathVariable final Long orderId,
            final MemberPrincipal memberPrincipal
    ) {
        final GetOrderUseCase.Input input = new GetOrderUseCase.Input(orderId, memberPrincipal.getMemberId());
        final GetOrderUseCase.Output output = getOrderUseCase.execute(input);
        return ApiResponse.success(output);
    }

    @Override
    @DeleteMapping("/{orderId}")
    public ApiResponse<Void> cancelOrder(
            @PathVariable final Long orderId,
            final MemberPrincipal memberPrincipal
    ) {
        final CancelOrderUseCase.Input input = new CancelOrderUseCase.Input(orderId, memberPrincipal.getMemberId());
        cancelOrderUseCase.execute(input);
        return ApiResponse.success();
    }
}
