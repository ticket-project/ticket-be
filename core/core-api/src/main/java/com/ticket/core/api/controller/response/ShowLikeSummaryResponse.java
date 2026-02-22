package com.ticket.core.api.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "찜한 공연 요약 응답")
public record ShowLikeSummaryResponse(

        @Schema(description = "공연 ID", example = "20")
        Long showId,

        @Schema(description = "공연 제목", example = "뮤지컬 위키드")
        String title,

        @Schema(description = "공연 포스터 이미지", example = "http://example.com/image.jpg")
        String image,

        @Schema(description = "공연 시작일", example = "2026-03-01")
        LocalDate startDate,

        @Schema(description = "공연 종료일", example = "2026-05-31")
        LocalDate endDate,

        @Schema(description = "공연장", example = "블루스퀘어")
        String venue,

        @Schema(description = "찜한 일시", example = "2026-02-17T10:00:00")
        LocalDateTime likedAt
) {
}
