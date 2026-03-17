package com.ticket.core.api.controller.docs;

import com.ticket.core.domain.member.MemberPrincipal;
import com.ticket.core.domain.order.command.usecase.TerminateOrderUseCase;
import com.ticket.core.domain.order.query.usecase.GetOrderDetailUseCase;
import com.ticket.core.support.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "주문", description = "PENDING 주문 조회/취소 API")
public interface OrderControllerDocs {

    @Operation(summary = "주문 조회", description = "주문/결제 화면 진입 시 호출합니다. OrderDetailResponse를 반환하며, 이미 만료된 주문은 EXPIRED 상태로 표시됩니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "주문 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "본인 주문이 아님")
    })
    ApiResponse<GetOrderDetailUseCase.Output> getOrder(
            @Parameter(description = "주문 키", example = "ORD-3f24c6bc355148f6bf941f0b2f2a6c2b", required = true) String orderKey,
            @Parameter(hidden = true) MemberPrincipal memberPrincipal
    );

    @Operation(summary = "주문 취소", description = "PENDING 주문과 연결된 HOLD를 취소합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "주문 취소 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "PENDING 주문이 아님")
    })
    ApiResponse<TerminateOrderUseCase.Output> cancelOrder(
            @Parameter(description = "주문 키", example = "ORD-3f24c6bc355148f6bf941f0b2f2a6c2b", required = true) String orderKey,
            @Parameter(hidden = true) MemberPrincipal memberPrincipal
    );
}
