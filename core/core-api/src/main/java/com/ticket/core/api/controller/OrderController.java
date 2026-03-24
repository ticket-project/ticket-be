package com.ticket.core.api.controller;

import com.ticket.core.api.controller.docs.OrderControllerDocs;
import com.ticket.core.api.controller.request.StartOrderRequest;
import com.ticket.core.domain.member.MemberPrincipal;
import com.ticket.core.domain.order.command.usecase.StartOrderUseCase;
import com.ticket.core.domain.order.command.usecase.TerminateOrderUseCase;
import com.ticket.core.domain.order.query.usecase.GetOrderDetailUseCase;
import com.ticket.core.support.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController implements OrderControllerDocs {

    private final StartOrderUseCase startOrderUseCase;
    private final GetOrderDetailUseCase getOrderDetailUseCase;
    private final TerminateOrderUseCase terminateOrderUseCase;

    @Override
    @PostMapping
    public ResponseEntity<ApiResponse<StartOrderUseCase.Output>> startOrder(
            @Valid @RequestBody final StartOrderRequest request,
            final MemberPrincipal memberPrincipal
    ) {
        final StartOrderUseCase.Input input = new StartOrderUseCase.Input(
                request.getPerformanceId(),
                request.getSeatIds(),
                memberPrincipal.getMemberId()
        );
        final StartOrderUseCase.Output output = startOrderUseCase.execute(input);
        return ResponseEntity.created(URI.create("/api/v1/orders/" + output.orderKey()))
                .header("X-Order-Key", output.orderKey())
                .body(ApiResponse.success(output));
    }

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
        terminateOrderUseCase.cancel(new TerminateOrderUseCase.Input(orderKey, memberPrincipal.getMemberId()));
        return ApiResponse.success();
    }
}
