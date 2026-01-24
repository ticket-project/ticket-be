package com.ticket.core.api.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;

@Schema(description = "공연 검색 요청 파라미터")
public class ShowSearchParam {

    @Schema(
            description = "카테고리 필터",
            example = "콘서트",
            allowableValues = {"뮤지컬", "콘서트", "연극", "오페라", "클래식"},
            requiredMode = NOT_REQUIRED
    )
    private String category;

    @Schema(
            description = "공연 장소 검색 (부분 일치)",
            example = "블루스퀘어",
            requiredMode = NOT_REQUIRED
    )
    private String place;

    @Schema(
            description = """
                    커서 값 (무한스크롤용)
                    - 첫 요청 시: 생략 또는 null
                    - 다음 페이지: 이전 응답의 `nextCursor` 값을 그대로 전달
                    """,
            example = "eyJpZCI6MTksInN0YXJ0RGF0ZSI6IjIwMjYtMDItMjgifQ==",
            requiredMode = NOT_REQUIRED
    )
    private String cursor;

    public ShowSearchParam(final String category, final String place, final String cursor) {
        this.category = category;
        this.place = place;
        this.cursor = cursor;
    }

    public String getCategory() {
        return category;
    }

    public String getPlace() {
        return place;
    }

    public String getCursor() {
        return cursor;
    }
}
