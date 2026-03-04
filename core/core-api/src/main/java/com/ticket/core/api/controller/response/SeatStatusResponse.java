package com.ticket.core.api.controller.response;

import com.ticket.core.enums.PerformanceSeatState;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "좌석 상태 응답 (회차별)")
public record SeatStatusResponse(

        @Schema(description = "좌석 상태 목록")
        List<SeatState> seats
) {
    @Schema(description = "개별 좌석 상태")
    public record SeatState(
            @Schema(description = "좌석 ID") Long seatId,
            @Schema(description = "좌석 상태 (AVAILABLE, HELD, RESERVED 등)") PerformanceSeatState state
    ) {}
}
