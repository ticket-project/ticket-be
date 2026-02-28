package com.ticket.core.api.controller.docs;

import com.ticket.core.api.controller.response.SeatAvailabilityResponse;
import com.ticket.core.support.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "공연 회차(Performance)", description = "공연 회차 관련 API")
public interface PerformanceControllerDocs {

    @Operation(
            summary = "회차별 등급별 잔여석 조회",
            description = """
                    특정 공연 회차의 등급별 잔여 좌석 수를 조회합니다.
                    등급 코드, 등급명, 가격, 총 좌석 수, 잔여 좌석 수를 반환합니다.
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    ApiResponse<SeatAvailabilityResponse> getSeatAvailability(
            @Parameter(description = "공연 회차 ID", example = "1", required = true) Long performanceId
    );
}
