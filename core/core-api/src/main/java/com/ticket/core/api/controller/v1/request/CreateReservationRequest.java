package com.ticket.core.api.controller.v1.request;

import com.ticket.core.domain.member.Member;
import com.ticket.core.domain.reservation.NewReservation;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public class CreateReservationRequest {
    @NotNull
    @Positive
    private Long performanceId;

    @NotEmpty
    private List<Long> seatIds;

    public Long getPerformanceId() {
        return performanceId;
    }

    public List<Long> getSeatIds() {
        return seatIds;
    }

    public NewReservation toNewReservation(final Member member) {
        return new NewReservation(member.getId(), performanceId, seatIds);
    }
}
