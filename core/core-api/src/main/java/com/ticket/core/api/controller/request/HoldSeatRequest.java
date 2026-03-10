package com.ticket.core.api.controller.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record HoldSeatRequest(
        @NotEmpty(message = "선점할 좌석을 선택해주세요.")
        List<Long> seatIds
) {}
