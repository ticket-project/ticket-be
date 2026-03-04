package com.ticket.core.api.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "선택된 회차 요약 정보")
public record PerformanceSummaryResponse(
        @Schema(description = "공연 제목", example = "싱어게인4 전국투어 콘서트 - 대전")
        String title,

        @Schema(description = "지역", example = "충청")
        String region,

        @Schema(description = "공연 일시", example = "2026-04-04T14:00:00")
        LocalDateTime startTime
) {
}
