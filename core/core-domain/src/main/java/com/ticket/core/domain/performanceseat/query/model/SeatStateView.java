package com.ticket.core.domain.performanceseat.query.model;

public record SeatStateView(
        Long seatId,
        SeatStatus status
) {
}
