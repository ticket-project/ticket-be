package com.ticket.core.api.controller;

import com.ticket.core.api.controller.docs.OrderControllerDocs;
import com.ticket.core.api.controller.response.OrderDetailResponse;
import com.ticket.core.domain.member.MemberPrincipal;
import com.ticket.core.domain.order.usecase.CancelOrderUseCase;
import com.ticket.core.domain.order.usecase.GetOrderUseCase;
import com.ticket.core.support.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController implements OrderControllerDocs {

    private final GetOrderUseCase getOrderUseCase;
    private final CancelOrderUseCase cancelOrderUseCase;

    @Override
    @GetMapping("/{orderId}")
    public ApiResponse<OrderDetailResponse> getOrder(
            @PathVariable final Long orderId,
            final MemberPrincipal memberPrincipal
    ) {
        final GetOrderUseCase.Output output = getOrderUseCase.execute(
                new GetOrderUseCase.Input(orderId, memberPrincipal.getMemberId())
        );
        return ApiResponse.success(output.order());
    }

    @Override
    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> cancelOrder(
            @PathVariable final Long orderId,
            final MemberPrincipal memberPrincipal
    ) {
        cancelOrderUseCase.execute(new CancelOrderUseCase.Input(orderId, memberPrincipal.getMemberId()));
        return ResponseEntity.noContent().build();
    }
}
