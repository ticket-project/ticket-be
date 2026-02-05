package com.ticket.core.api.controller.response;

import com.ticket.core.domain.show.Region;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/**
 * 공연 검색 결과 응답 DTO
 * 요구사항: 이름, 썸네일이미지, 장소, 날짜, 지역
 */
@Schema(description = "공연 검색 결과")
public record ShowSearchResponse(

        @Schema(description = "공연 ID", example = "20")
        Long id,

        @Schema(description = "공연명", example = "뮤지컬 위키드")
        String title,

        @Schema(description = "썸네일 이미지 URL", example = "http://example.com/image.jpg")
        String image,

        @Schema(description = "장소", example = "블루스퀘어 신한카드홀")
        String venue,

        @Schema(description = "공연 시작일", example = "2026-03-01")
        LocalDate startDate,

        @Schema(description = "공연 종료일", example = "2026-05-31")
        LocalDate endDate,

        @Schema(description = "지역 (한글명)", example = "서울")
        Region region,

        @Schema(description = "조회수 (정렬 기준)", example = "15000")
        Long viewCount
) {
}
