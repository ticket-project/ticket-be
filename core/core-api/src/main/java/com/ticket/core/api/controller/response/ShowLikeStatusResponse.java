package com.ticket.core.api.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "공연 찜 상태 응답")
public record ShowLikeStatusResponse(

        @Schema(description = "공연 ID", example = "20")
        Long showId,

        @Schema(description = "찜 여부", example = "true")
        boolean liked
) {
}
