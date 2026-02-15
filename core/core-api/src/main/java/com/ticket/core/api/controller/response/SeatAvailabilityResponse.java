package com.ticket.core.api.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "회차별 등급별 잔여석 응답")
public record SeatAvailabilityResponse(

        @Schema(description = "등급별 잔여석 목록")
        List<GradeAvailability> grades
) {

    @Schema(description = "등급별 잔여석")
    public record GradeAvailability(
            @Schema(description = "등급 이름") String gradeName,
            @Schema(description = "정렬 순서") int sortOrder,
            @Schema(description = "잔여 좌석 수") long availableSeats
    ) {}
}
