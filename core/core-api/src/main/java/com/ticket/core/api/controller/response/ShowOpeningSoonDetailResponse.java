package com.ticket.core.api.controller.response;

import com.ticket.core.domain.show.Region;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.Arrays;

/**
 * 더보기 페이지용 판매 오픈 예정 공연 상세 응답 (무한스크롤)
 */
@Schema(description = "더보기 페이지용 판매 오픈 예정 공연 상세 정보")
public record ShowOpeningSoonDetailResponse(

        @Schema(description = "공연 ID", example = "20")
        Long id,

        @Schema(description = "공연 제목", example = "뮤지컬 위키드")
        String title,

        @Schema(description = "공연 부제목", example = "10주년 기념 공연")
        String subTitle,

        @Schema(description = "공연 썸네일 이미지", example = "http://example.com/image.jpg")
        String image,

        @Schema(description = "장소", example = "예술의전당")
        String venue,

        @Schema(description = "지역", example = "서울")
        String region,

        @Schema(description = "공연 시작일", example = "2026-03-01")
        LocalDate startDate,

        @Schema(description = "공연 종료일", example = "2026-08-01")
        LocalDate endDate,

        @Schema(description = "티켓 판매 시작일", example = "2026-02-01")
        LocalDate saleStartDate,

        @Schema(description = "티켓 판매 종료일", example = "2026-07-21")
        LocalDate saleEndDate,

        @Schema(description = "조회수 (인기순 정렬 기준)", example = "15000")
        Long viewCount
) {
    /**
     * Region enum 코드를 한글 이름으로 변환
     */
    private static String toKoreanRegion(String regionCode) {
        if (regionCode == null) return null;
        return Arrays.stream(Region.values())
                .filter(r -> r.name().equals(regionCode))
                .findFirst()
                .map(Region::getDescription)
                .orElse(regionCode);
    }
    
    /**
     * 정적 팩토리 메서드 - region 코드를 한글로 변환
     */
    public static ShowOpeningSoonDetailResponse of(
            Long id,
            String title,
            String subTitle,
            String image,
            String venue,
            String regionCode,
            LocalDate startDate,
            LocalDate endDate,
            LocalDate saleStartDate,
            LocalDate saleEndDate,
            Long viewCount
    ) {
        return new ShowOpeningSoonDetailResponse(
                id, title, subTitle, image, venue,
                toKoreanRegion(regionCode),
                startDate, endDate, saleStartDate, saleEndDate, viewCount
        );
    }
}
