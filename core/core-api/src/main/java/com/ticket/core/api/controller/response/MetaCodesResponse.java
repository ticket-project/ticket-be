package com.ticket.core.api.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "프론트 공통 코드(카테고리/장르/Enum) 조회 응답")
public record MetaCodesResponse(

        @Schema(description = "카테고리 코드 목록")
        List<CategoryCodeItem> category,

        @Schema(description = "장르 코드 목록")
        List<GenreCodeItem> genres,

        @Schema(description = "Enum 코드 목록")
        EnumCodes enums
) {

    @Schema(description = "카테고리 코드 항목")
    public record CategoryCodeItem(
            @Schema(description = "카테고리 ID", example = "1") Long id,
            @Schema(description = "카테고리 코드", example = "CONCERT") String code,
            @Schema(description = "카테고리 이름", example = "콘서트") String name
    ) {}

    @Schema(description = "장르 코드 항목")
    public record GenreCodeItem(
            @Schema(description = "장르 ID", example = "1") Long id,
            @Schema(description = "상위 카테고리 코드", example = "CONCERT") String categoryCode,
            @Schema(description = "장르 코드", example = "KPOP") String code,
            @Schema(description = "장르 이름", example = "케이팝") String name
    ) {}

    @Schema(description = "Enum 묶음")
    public record EnumCodes(
            @Schema(description = "예매 상태") List<EnumCodeItem> bookingStatus,
            @Schema(description = "회차 상태") List<EnumCodeItem> performanceState,
            @Schema(description = "회차 좌석 상태") List<EnumCodeItem> performanceSeatState,
            @Schema(description = "선점 상태") List<EnumCodeItem> holdState,
            @Schema(description = "주문 상태") List<EnumCodeItem> orderState,
            @Schema(description = "소셜 제공자") List<EnumCodeItem> socialProvider,
            @Schema(description = "회원 권한") List<EnumCodeItem> role,
            @Schema(description = "엔티티 상태") List<EnumCodeItem> entityStatus,
            @Schema(description = "판매 타입") List<EnumCodeItem> saleType,
            @Schema(description = "판매 상태") List<EnumCodeItem> saleStatus,
            @Schema(description = "지역") List<EnumCodeItem> region,
            @Schema(description = "공연 정렬 타입") List<EnumCodeItem> showSortKey
    ) {}

    @Schema(description = "Enum 코드 항목")
    public record EnumCodeItem(
            @Schema(description = "코드", example = "ON_SALE") String code,
            @Schema(description = "설명", example = "예매중") String description
    ) {}
}
