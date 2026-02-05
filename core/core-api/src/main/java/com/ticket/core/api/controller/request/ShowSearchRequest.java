package com.ticket.core.api.controller.request;

import com.ticket.core.domain.show.Region;
import com.ticket.core.domain.show.SaleStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;

/**
 * 공연 검색 API 요청 파라미터
 */
@Schema(description = "공연 검색 요청 파라미터")
public class ShowSearchRequest {

    @Schema(
            description = "검색어 (공연명 검색)",
            example = "뮤지컬",
            requiredMode = NOT_REQUIRED
    )
    private String keyword;

    @Schema(
            description = "카테고리 필터",
            example = "CONCERT",
            allowableValues = {"CONCERT", "THEATER"},
            requiredMode = NOT_REQUIRED
    )
    private String category;

    @Schema(
            description = "판매 상태 필터",
            example = "ON_SALE",
            allowableValues = {"UPCOMING", "ON_SALE", "CLOSED"},
            requiredMode = NOT_REQUIRED
    )
    private SaleStatus saleStatus;

    @Schema(
            description = "공연 시작일 필터 (이 날짜 이후)",
            example = "2026-03-01",
            requiredMode = NOT_REQUIRED
    )
    private LocalDate startDateFrom;

    @Schema(
            description = "공연 시작일 필터 (이 날짜 이전)",
            example = "2026-06-01",
            requiredMode = NOT_REQUIRED
    )
    private LocalDate startDateTo;

    @Schema(
            description = "지역 필터",
            example = "SEOUL",
            requiredMode = NOT_REQUIRED
    )
    private Region region;

    @Schema(
            description = """
                    커서 값 (무한스크롤용)
                    - 첫 요청 시: 생략 또는 null
                    - 다음 페이지: 이전 응답의 `nextCursor` 값을 그대로 전달
                    """,
            example = "eyJpZCI6MTksInZpZXdDb3VudCI6MTUwMDB9",
            requiredMode = NOT_REQUIRED
    )
    private String cursor;

    public ShowSearchRequest(
            String keyword,
            String category,
            SaleStatus saleStatus,
            LocalDate startDateFrom,
            LocalDate startDateTo,
            Region region,
            String cursor
    ) {
        this.keyword = keyword;
        this.category = category;
        this.saleStatus = saleStatus;
        this.startDateFrom = startDateFrom;
        this.startDateTo = startDateTo;
        this.region = region;
        this.cursor = cursor;
    }

    public String getKeyword() {
        return keyword;
    }

    public String getCategory() {
        return category;
    }

    public SaleStatus getSaleStatus() {
        return saleStatus;
    }

    public LocalDate getStartDateFrom() {
        return startDateFrom;
    }

    public LocalDate getStartDateTo() {
        return startDateTo;
    }

    public Region getRegion() {
        return region;
    }

    public String getCursor() {
        return cursor;
    }
}
