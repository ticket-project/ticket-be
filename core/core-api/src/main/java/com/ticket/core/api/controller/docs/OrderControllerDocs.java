package com.ticket.core.api.controller.docs;

import com.ticket.core.domain.member.MemberPrincipal;
import com.ticket.core.domain.order.usecase.GetOrderUseCase;
import com.ticket.core.support.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "주문", description = "PENDING 주문 조회/취소 API")
public interface OrderControllerDocs {

    @Operation(summary = "주문 조회", description = "주문/결제 화면 진입 시 호출합니다. OrderDetailResponse를 반환하며, 만료된 PENDING 주문은 조회 시 EXPIRED로 즉시 반영됩니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "주문 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "본인 주문이 아님")
    })
    ApiResponse<GetOrderUseCase.Output> getOrder(
            @Parameter(description = "주문 ID", example = "1001", required = true) Long orderId,
            @Parameter(hidden = true) MemberPrincipal memberPrincipal
    );

    @Operation(summary = "주문 취소", description = "PENDING 주문과 연결된 HOLD 를 취소합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "주문 취소 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "PENDING 주문이 아님")
    })
    ApiResponse<Void> cancelOrder(
            @Parameter(description = "주문 ID", example = "1001", required = true) Long orderId,
            @Parameter(hidden = true) MemberPrincipal memberPrincipal
    );
}
