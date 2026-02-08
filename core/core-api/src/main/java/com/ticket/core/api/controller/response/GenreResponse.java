package com.ticket.core.api.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 장르 정보 응답 DTO
 */
@Schema(description = "장르 정보 응답")
public record GenreResponse(

        @Schema(description = "장르 ID", example = "1")
        Long id,

        @Schema(description = "장르 코드", example = "KPOP")
        String code,

        @Schema(description = "장르 이름", example = "K-POP")
        String name
) {
}
