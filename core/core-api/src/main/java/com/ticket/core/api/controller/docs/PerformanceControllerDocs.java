package com.ticket.core.api.controller.docs;

import com.ticket.core.api.controller.response.PerformanceScheduleListResponse;
import com.ticket.core.api.controller.response.PerformanceSummaryResponse;
import com.ticket.core.api.controller.response.SeatAvailabilityResponse;
import com.ticket.core.api.controller.response.SeatStatusResponse;
import com.ticket.core.support.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "공연 회차(Performance)", description = "공연 회차 관련 API")
public interface PerformanceControllerDocs {

    @Operation(
            summary = "회차 요약 정보 조회",
            description = """
                    선택된 회차의 요약 정보를 조회합니다.
                    제목, 지역, 회차 시작 일시를 반환합니다.
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    ApiResponse<PerformanceSummaryResponse> getPerformanceSummary(
            @Parameter(description = "공연 회차 ID", example = "1", required = true) Long performanceId
    );

    @Operation(
            summary = "일정 변경용 회차 목록 조회",
            description = """
                    선택된 회차와 같은 공연에 속한 회차 목록을 조회합니다.
                    일정변경 버튼 클릭 시 사용할 수 있습니다.
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    ApiResponse<PerformanceScheduleListResponse> getPerformanceSchedules(
            @Parameter(description = "공연 회차 ID", example = "1", required = true) Long performanceId
    );

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

    @Operation(
            summary = "회차별 좌석 상태 조회",
            description = """
                    특정 회차의 모든 좌석 상태(AVAILABLE, HELD, RESERVED 등)를 조회합니다.
                    실시간으로 변동될 수 있는 데이터입니다.
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    ApiResponse<SeatStatusResponse> getSeatStatus(
            @Parameter(description = "공연 회차 ID", example = "1", required = true) Long performanceId
    );
}
