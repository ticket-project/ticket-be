package com.ticket.core.support.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Slice;

import java.util.List;

@Schema(description = "무한스크롤 페이지네이션 응답")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SliceResponse<T>(

        @Schema(description = "조회된 데이터 목록")
        List<T> items,

        @Schema(description = "다음 페이지 존재 여부 (false이면 마지막 페이지)", example = "true")
        boolean hasNext,

        @Schema(description = "요청한 페이지 크기", example = "5")
        int size,

        @Schema(description = "실제 반환된 데이터 개수", example = "5")
        int numberOfElements,

        @Schema(
                description = """
                        다음 페이지 요청을 위한 커서 값
                        - 다음 요청 시 `cursor` 파라미터에 이 값을 전달
                        - `hasNext`가 false이면 null
                        """,
                example = "eyJpZCI6MTksInN0YXJ0RGF0ZSI6IjIwMjYtMDItMjgifQ=="
        )
        String nextCursor
) {
    public static <T> SliceResponse<T> from(final Slice<T> slice, final String nextCursor) {
        return new SliceResponse<>(
                slice.getContent(),
                slice.hasNext(),
                slice.getSize(),
                slice.getNumberOfElements(),
                nextCursor
        );
    }
}
