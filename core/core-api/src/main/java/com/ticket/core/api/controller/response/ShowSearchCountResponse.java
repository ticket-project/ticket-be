package com.ticket.core.api.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 공연 검색 결과 개수 응답 DTO
 * 필터 선택 시 실제 데이터 없이 개수만 반환
 */
@Schema(description = "공연 검색 결과 개수")
public record ShowSearchCountResponse(

        @Schema(description = "검색 결과 개수", example = "42")
        long count
) {
}
