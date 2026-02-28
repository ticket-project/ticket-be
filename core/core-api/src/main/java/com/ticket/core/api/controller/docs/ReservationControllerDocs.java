package com.ticket.core.api.controller.docs;

import com.ticket.core.api.controller.request.AddReservationRequest;
import com.ticket.core.domain.member.MemberPrincipal;
import com.ticket.core.support.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "예약(Reservation)", description = "좌석 예약 API")
public interface ReservationControllerDocs {

    @Operation(summary = "좌석 예약", description = "선점한 좌석을 최종 예약합니다")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "예약 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "좌석 충돌 (이미 예약됨)")
    })
    ApiResponse<Void> reserveV1(
            @Parameter(hidden = true) MemberPrincipal memberPrincipal,
            AddReservationRequest request
    );
}
