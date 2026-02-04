package com.ticket.core.api.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;

/**
 * 판매 오픈 예정 공연 목록 조회 요청 파라미터
 */
@Schema(description = "판매 오픈 예정 공연 조회 요청 파라미터")
public class SaleOpeningSoonSearchParam {

    @Schema(
            description = "카테고리 필터",
            example = "CONCERT",
            allowableValues = {"CONCERT", "THEATER"},
            requiredMode = NOT_REQUIRED
    )
    private String category;

    @Schema(
            description = "공연 제목 검색 (부분 일치)",
            example = "뮤지컬",
            requiredMode = NOT_REQUIRED
    )
    private String title;

    @Schema(
            description = "판매 시작일 필터 (이 날짜 이후)",
            example = "2026-03-01",
            requiredMode = NOT_REQUIRED
    )
    private LocalDate saleStartDateFrom;

    @Schema(
            description = "판매 시작일 필터 (이 날짜 이전)",
            example = "2026-04-01",
            requiredMode = NOT_REQUIRED
    )
    private LocalDate saleStartDateTo;

    @Schema(
            description = "판매 종료일 필터 (이 날짜 이후)",
            example = "2026-05-01",
            requiredMode = NOT_REQUIRED
    )
    private LocalDate saleEndDateFrom;

    @Schema(
            description = "판매 종료일 필터 (이 날짜 이전)",
            example = "2026-06-01",
            requiredMode = NOT_REQUIRED
    )
    private LocalDate saleEndDateTo;

    @Schema(
            description = """
                    커서 값 (무한스크롤용)
                    - 첫 요청 시: 생략 또는 null
                    - 다음 페이지: 이전 응답의 `nextCursor` 값을 그대로 전달
                    """,
            example = "eyJpZCI6MTksInNhbGVTdGFydERhdGUiOiIyMDI2LTAzLTAxIn0=",
            requiredMode = NOT_REQUIRED
    )
    private String cursor;

    public SaleOpeningSoonSearchParam() {}

    public SaleOpeningSoonSearchParam(
            String category,
            String title,
            LocalDate saleStartDateFrom,
            LocalDate saleStartDateTo,
            LocalDate saleEndDateFrom,
            LocalDate saleEndDateTo,
            String cursor
    ) {
        this.category = category;
        this.title = title;
        this.saleStartDateFrom = saleStartDateFrom;
        this.saleStartDateTo = saleStartDateTo;
        this.saleEndDateFrom = saleEndDateFrom;
        this.saleEndDateTo = saleEndDateTo;
        this.cursor = cursor;
    }

    public String getCategory() {
        return category;
    }

    public String getTitle() {
        return title;
    }

    public LocalDate getSaleStartDateFrom() {
        return saleStartDateFrom;
    }

    public LocalDate getSaleStartDateTo() {
        return saleStartDateTo;
    }

    public LocalDate getSaleEndDateFrom() {
        return saleEndDateFrom;
    }

    public LocalDate getSaleEndDateTo() {
        return saleEndDateTo;
    }

    public String getCursor() {
        return cursor;
    }
}
