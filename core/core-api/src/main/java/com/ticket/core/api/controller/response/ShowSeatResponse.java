package com.ticket.core.api.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;


@Schema(description = "공연 좌석 정보 응답 (등급 + 좌석 좌표)")
public record ShowSeatResponse(

        @Schema(description = "좌석 목록")
        List<SeatInfo> seats
) {

    @Schema(description = "좌석 정보")
    public record SeatInfo(
            @Schema(description = "좌석 ID") Long seatId,
            @Schema(description = "층") int floor,
            @Schema(description = "구역") String section,
            @Schema(description = "행") String row,
            @Schema(description = "열") String col,
            @Schema(description = "SVG x좌표") double x,
            @Schema(description = "SVG y좌표") double y,
            @Schema(description = "등급 코드") String gradeCode,
            @Schema(description = "등급 이름") String gradeName,
            @Schema(description = "가격") BigDecimal price
    ) {}
}
