package com.ticket.core.api.controller.response;

import com.ticket.core.domain.show.Region;
import com.ticket.core.domain.show.SaleType;
import com.ticket.core.enums.BookingStatus;
import com.ticket.core.enums.PerformanceState;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "공연 상세 정보 응답")
public record ShowDetailResponse(

        @Schema(description = "공연 ID")
        Long id,

        @Schema(description = "공연 제목")
        String title,

        @Schema(description = "공연 부제목")
        String subTitle,

        @Schema(description = "공연 상세 정보")
        String info,

        @Schema(description = "공연 시작일")
        LocalDate startDate,

        @Schema(description = "공연 종료일")
        LocalDate endDate,
        @Schema(description = "Running time in minutes")
        Integer runningMinutes,

        @Schema(description = "조회수")
        Long viewCount,

        @Schema(description = "판매 타입", example = "{\"code\":\"GENERAL\",\"name\":\"일반판매\"}")
        SaleType saleType,

        @Schema(description = "판매 시작일")
        LocalDate saleStartDate,

        @Schema(description = "판매 종료일")
        LocalDate saleEndDate,

        @Schema(description = "포스터 이미지 URL")
        String image,

        @Schema(description = "공연장 정보")
        VenueInfo venue,

        @Schema(description = "출연자 정보")
        PerformerInfo performer,

        @Schema(description = "장르 목록")
        List<String> genreNames,

        @Schema(description = "좌석 등급 및 가격 목록")
        List<GradeInfo> grades,

        @Schema(description = "공연 날짜별 회차 목록")
        List<PerformanceDateInfo> performanceDates
) {

    @Schema(description = "출연자 정보")
    public record PerformerInfo(
            @Schema(description = "출연자 ID") Long id,
            @Schema(description = "출연자 이름") String name,
            @Schema(description = "프로필 이미지 URL") String profileImageUrl
    ) {}

    @Schema(description = "좌석 등급 및 가격 정보")
    public record GradeInfo(
            @Schema(description = "등급 ID") Long id,
            @Schema(description = "등급 코드") String gradeCode,
            @Schema(description = "등급 이름") String gradeName,
            @Schema(description = "가격") BigDecimal price,
            @Schema(description = "정렬 순서") Integer sortOrder
    ) {}

    @Schema(description = "공연 회차 정보")
    public record PerformanceInfo(
            @Schema(description = "회차 ID") Long id,
            @Schema(description = "회차 번호") Long performanceNo,
            @Schema(description = "공연 시작 시간") LocalDateTime startTime,
            @Schema(description = "공연 종료 시간") LocalDateTime endTime,
            @Schema(description = "예매 오픈 시간") LocalDateTime orderOpenTime,
            @Schema(description = "예매 마감 시간") LocalDateTime orderCloseTime,
            @Schema(
                    description = "회차 상태 (OPEN: 예매 오픈, CLOSE: 예매 마감)",
                    allowableValues = {"OPEN", "CLOSE"},
                    example = "OPEN"
            ) PerformanceState state,
            @Schema(
                    description = "예매 상태 (BEFORE_OPEN: 오픈 전, ON_SALE: 예매 중, CLOSED: 마감)",
                    allowableValues = {"BEFORE_OPEN", "ON_SALE", "CLOSED"},
                    example = "ON_SALE"
            ) BookingStatus bookingStatus
    ) {}

    @Schema(description = "공연 날짜별 회차 정보")
    public record PerformanceDateInfo(
            @Schema(description = "공연 날짜") LocalDate date,
            @Schema(description = "해당 날짜 회차 목록") List<PerformanceInfo> performances
    ) {}

    @Schema(description = "공연장 정보")
    public record VenueInfo(
            @Schema(description = "공연장 ID") Long id,
            @Schema(description = "공연장 이름") String name,
            @Schema(description = "공연장 주소") String address,
            @Schema(description = "지역") Region region,
            @Schema(description = "위도") BigDecimal latitude,
            @Schema(description = "경도") BigDecimal longitude,
            @Schema(description = "전화번호") String phone,
            @Schema(description = "공연장 이미지") String imageUrl
    ) {}
}
