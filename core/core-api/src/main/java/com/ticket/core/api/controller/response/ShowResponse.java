package com.ticket.core.api.controller.response;

import com.ticket.core.domain.show.Region;
import com.ticket.core.domain.show.SaleType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "공연 정보 응답")
public record ShowResponse(

        @Schema(description = "공연 ID", example = "20")
        Long id,

        @Schema(description = "공연 제목", example = "뮤지컬 위키드")
        String title,

        @Schema(description = "공연 부제목", example = "10주년 기념 공연")
        String subTitle,

        @Schema(description = "장르 이름 목록", example = "[\"힙합\", \"R&B\"]")
        List<String> genreNames,

        @Schema(description = "공연 시작일", example = "2026-03-01")
        LocalDate startDate,

        @Schema(description = "공연 종료일", example = "2026-05-31")
        LocalDate endDate,

        @Schema(description = "조회수", example = "15000")
        Long viewCount,

        @Schema(description = "판매 타입", example = "{\"code\":\"GENERAL\",\"name\":\"일반판매\"}")
        SaleType saleType,

        @Schema(description = "판매 시작일", example = "2026-01-01")
        LocalDate saleStartDate,

        @Schema(description = "판매 종료일", example = "2026-02-28")
        LocalDate saleEndDate,

        @Schema(description = "생성일", example = "2026-02-28T14:00:00")
        LocalDateTime createdAt,

        @Schema(description = "지역")
        Region region,

        @Schema(description = "장소", example = "예술의전당")
        String venue
) {
}
