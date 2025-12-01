package com.ticket.core.api.controller.v1.request;

import com.ticket.core.domain.reservation.NewReservation;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class CreateReservationRequest {

    private Long memberId;

    @NotEmpty
    private List<Long> seatIds;

    public Long getMemberId() {
        return memberId;
    }

    public List<Long> getSeatIds() {
        return seatIds;
    }

    public NewReservation toNewReservation(final Long performanceId) {
        return new NewReservation(memberId, performanceId, seatIds);
    }
}
