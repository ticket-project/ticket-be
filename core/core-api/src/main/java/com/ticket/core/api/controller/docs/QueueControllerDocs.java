package com.ticket.core.api.controller.docs;

import com.ticket.core.api.controller.response.QueueEntryResponse;
import com.ticket.core.api.controller.response.QueueStatusResponse;
import com.ticket.core.support.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "대기열", description = "회차별 좌석 페이지 진입 대기열 API")
public interface QueueControllerDocs {

    @Operation(
            summary = "대기열 진입",
            description = """
                    회차 좌석 페이지 진입 전 대기열에 진입합니다.
                    즉시 입장 가능하면 토큰을 발급하고, 아니면 현재 순번을 반환합니다.
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "진입 처리 성공")
    })
    ApiResponse<QueueEntryResponse> enter(
            @Parameter(description = "회차 ID", example = "1", required = true) Long performanceId
    );

    @Operation(
            summary = "대기열 상태 조회",
            description = "현재 대기 상태 또는 입장 토큰 상태를 조회합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "상태 조회 성공")
    })
    ApiResponse<QueueStatusResponse> getStatus(
            @Parameter(description = "회차 ID", example = "1", required = true) Long performanceId,
            @Parameter(description = "대기열 엔트리 ID", example = "qe_1234", required = true) String queueEntryId
    );

    @Operation(
            summary = "대기열 이탈",
            description = "현재 대기 또는 입장 상태를 종료합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "이탈 성공")
    })
    ApiResponse<Void> leave(
            @Parameter(description = "회차 ID", example = "1", required = true) Long performanceId,
            @Parameter(description = "대기열 엔트리 ID", example = "qe_1234", required = true) String queueEntryId
    );
}
