package com.ticket.core.api.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/**
 * 메인 화면용 판매 오픈 예정 공연 요약 응답 (5개)
 */
@Schema(description = "메인 화면용 판매 오픈 예정 공연 요약 정보")
public record ShowOpeningSoonSummaryResponse(

        @Schema(description = "공연 ID", example = "20")
        Long id,

        @Schema(description = "공연 제목", example = "뮤지컬 위키드")
        String title,

        @Schema(description = "공연 썸네일 이미지", example = "http://example.com/image.jpg")
        String image,

        @Schema(description = "장소", example = "예술의전당")
        String venue,

        @Schema(description = "티켓 판매 시작일", example = "2026-02-01")
        LocalDate saleStartDate
) {}
