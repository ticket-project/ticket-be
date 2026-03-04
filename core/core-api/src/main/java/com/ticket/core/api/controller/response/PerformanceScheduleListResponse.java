package com.ticket.core.api.controller.response;

import com.ticket.core.enums.PerformanceState;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "일정 변경용 회차 목록 응답")
public record PerformanceScheduleListResponse(
        @Schema(description = "공연 ID", example = "1")
        Long showId,

        @Schema(description = "현재 선택된 회차 ID", example = "101")
        Long selectedPerformanceId,

        @Schema(description = "같은 공연의 회차 목록")
        List<PerformanceScheduleItem> schedules
) {

    @Schema(description = "회차 목록 항목")
    public record PerformanceScheduleItem(
            @Schema(description = "회차 ID", example = "101")
            Long performanceId,

            @Schema(description = "회차 번호", example = "1")
            Long performanceNo,

            @Schema(description = "회차 시작 일시", example = "2026-04-04T14:00:00")
            LocalDateTime startTime,

            @Schema(description = "회차 상태", example = "OPEN")
            PerformanceState state
    ) {
    }
}
