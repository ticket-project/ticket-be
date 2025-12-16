package com.ticket.core.api.controller.v1.request;

import com.ticket.core.domain.reservation.NewReservation;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public class AddReservationRequest {

    @Positive
    @NotNull
    private Long performanceId;

    @NotEmpty
    private List<@Positive @NotNull Long> seatIds;

    public AddReservationRequest() {}

    public Long getPerformanceId() {
        return performanceId;
    }

    public List<Long> getSeatIds() {
        return seatIds;
    }

    public NewReservation toNewReservation(final Long memberId) {
        return new NewReservation(memberId, performanceId, seatIds);
    }
}
