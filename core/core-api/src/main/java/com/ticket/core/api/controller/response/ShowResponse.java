package com.ticket.core.api.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "공연 정보 응답")
public record ShowResponse(

        @Schema(description = "공연 ID", example = "20")
        Long id,

        @Schema(description = "공연 제목", example = "뮤지컬 위키드")
        String title,

        @Schema(description = "공연 부제목", example = "10주년 기념 공연")
        String subTitle,

        @Schema(description = "공연 시작일", example = "2026-03-01")
        LocalDate startDate,

        @Schema(description = "공연 종료일", example = "2026-05-31")
        LocalDate endDate,

        @Schema(description = "조회수", example = "15000")
        Long viewCount,

        @Schema(description = "공연 장소", example = "블루스퀘어 신한카드홀")
        String place
) {
}
