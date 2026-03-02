package com.ticket.core.api.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "공연장 레이아웃 응답")
public record VenueLayoutResponse(
        @Schema(description = "공연장 이름") String name,
        @Schema(description = "SVG viewBox 가로") int viewBoxWidth,
        @Schema(description = "SVG viewBox 세로") int viewBoxHeight,
        @Schema(description = "좌석 지름") double seatDiameter
) {}
