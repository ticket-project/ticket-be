package com.ticket.core.api.controller.docs;

import com.ticket.core.api.controller.request.HoldSeatRequest;
import com.ticket.core.api.controller.response.HoldSeatResponse;
import com.ticket.core.domain.member.MemberPrincipal;
import com.ticket.core.support.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "좌석 선점", description = "좌석 선점(Hold)/해제 API")
public interface SeatHoldControllerDocs {

    @Operation(
            summary = "좌석 선점",
            description = """
                    선택한 좌석들을 선점(Hold)하고 PENDING 주문을 생성합니다.
                    성공 시 주문 정보(orderId, orderNo, totalAmount)와 함께 WebSocket HELD 이벤트가 브로드캐스트됩니다.
                    holdTime(기본 300초) 후 자동 만료되며, 만료 시 PENDING 주문도 자동 취소됩니다.
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "좌석 선점 및 주문 생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 선점된 좌석"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "예매 오픈 전 또는 최대 수량 초과")
    })
    ApiResponse<HoldSeatResponse> holdSeats(
            @Parameter(description = "공연 회차 ID", example = "1", required = true) Long performanceId,
            HoldSeatRequest request,
            @Parameter(hidden = true) MemberPrincipal memberPrincipal
    );

    @Operation(
            summary = "좌석 선점 해제",
            description = """
                    선점한 좌석들을 해제합니다.
                    본인이 Hold한 좌석만 해제할 수 있으며, 성공 시 RELEASED 이벤트가 브로드캐스트됩니다.
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "선점 해제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "본인이 선점한 좌석이 아님")
    })
    ApiResponse<Void> releaseSeats(
            @Parameter(description = "공연 회차 ID", example = "1", required = true) Long performanceId,
            HoldSeatRequest request,
            @Parameter(hidden = true) MemberPrincipal memberPrincipal
    );
}

