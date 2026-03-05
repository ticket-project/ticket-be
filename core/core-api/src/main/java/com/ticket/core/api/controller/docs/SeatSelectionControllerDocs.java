package com.ticket.core.api.controller.docs;

import com.ticket.core.domain.member.MemberPrincipal;
import com.ticket.core.support.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "좌석 선택", description = "좌석 선택/해제 API (실시간 알림은 WebSocket 구독)")
public interface SeatSelectionControllerDocs {

    @Operation(
            summary = "좌석 선택",
            description = """
                    특정 좌석을 임시 선택(잠금)합니다.
                    Redis SET NX로 원자적으로 잠금하며, 성공 시 WebSocket 구독자에게 SELECTED 이벤트가 브로드캐스트됩니다.
                    5분 후 자동 만료됩니다.
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "좌석 선택 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 선택된 좌석")
    })
    ApiResponse<Void> selectSeat(
            @Parameter(description = "공연 회차 ID", example = "1", required = true) Long performanceId,
            @Parameter(description = "좌석 ID", example = "42", required = true) Long seatId,
            @Parameter(hidden = true) MemberPrincipal memberPrincipal
    );

    @Operation(
            summary = "좌석 선택 해제",
            description = """
                    선택한 좌석을 해제합니다.
                    본인이 선택한 좌석만 해제할 수 있으며, 성공 시 DESELECTED 이벤트가 브로드캐스트됩니다.
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "좌석 해제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "본인이 선택한 좌석이 아님")
    })
    ApiResponse<Void> deselectSeat(
            @Parameter(description = "공연 회차 ID", example = "1", required = true) Long performanceId,
            @Parameter(description = "좌석 ID", example = "42", required = true) Long seatId,
            @Parameter(hidden = true) MemberPrincipal memberPrincipal
    );
}
