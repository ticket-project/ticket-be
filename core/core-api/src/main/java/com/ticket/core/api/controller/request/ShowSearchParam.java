package com.ticket.core.api.controller.request;

import com.ticket.core.domain.show.Region;
import io.swagger.v3.oas.annotations.media.Schema;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;

@Schema(description = "공연 조회 요청 파라미터")
public class ShowSearchParam {

    @Schema(
            description = "카테고리 필터",
            example = "CONCERT",
            allowableValues = {"CONCERT", "THEATER"},
            requiredMode = NOT_REQUIRED
    )
    private String category;

    @Schema(
            description = "지역 검색",
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
            example = "eyJpZCI6MTksInN0YXJ0RGF0ZSI6IjIwMjYtMDItMjgifQ==",
            requiredMode = NOT_REQUIRED
    )
    private String cursor;

    public ShowSearchParam(final String category, final Region region, final String cursor) {
        this.category = category;
        this.region = region;
        this.cursor = cursor;
    }

    public String getCategory() {
        return category;
    }

    public Region getRegion() {
        return region;
    }

    public String getCursor() {
        return cursor;
    }
}
