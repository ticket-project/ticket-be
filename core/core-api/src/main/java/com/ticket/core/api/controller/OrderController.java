package com.ticket.core.api.controller;

import com.ticket.core.api.controller.docs.OrderControllerDocs;
import com.ticket.core.domain.member.MemberPrincipal;
import com.ticket.core.domain.order.command.usecase.TerminateOrderUseCase;
import com.ticket.core.domain.order.query.usecase.GetOrderDetailUseCase;
import com.ticket.core.support.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController implements OrderControllerDocs {

    private final GetOrderDetailUseCase getOrderDetailUseCase;
    private final TerminateOrderUseCase terminateOrderUseCase;

    @Override
    @GetMapping("/{orderKey}")
    public ApiResponse<GetOrderDetailUseCase.Output> getOrder(
            @PathVariable final String orderKey,
            final MemberPrincipal memberPrincipal
    ) {
        final GetOrderDetailUseCase.Input input = new GetOrderDetailUseCase.Input(orderKey, memberPrincipal.getMemberId());
        final GetOrderDetailUseCase.Output output = getOrderDetailUseCase.execute(input);
        return ApiResponse.success(output);
    }

    @Override
    @DeleteMapping("/{orderKey}")
    public ApiResponse<Void> cancelOrder(
            @PathVariable final String orderKey,
            final MemberPrincipal memberPrincipal
    ) {
        terminateOrderUseCase.cancel(orderKey, memberPrincipal.getMemberId(), LocalDateTime.now());
        return ApiResponse.success();
    }
}
