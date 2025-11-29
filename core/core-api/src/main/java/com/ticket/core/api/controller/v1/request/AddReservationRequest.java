package com.ticket.core.api.controller.v1.request;

import com.ticket.core.domain.reservation.AddReservation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.util.List;

public class AddReservationRequest {
    @NotBlank @Positive
    private Long memberId;

    @NotBlank @Positive
    private Long performanceId;

    @NotBlank
    private List<Long> seatIds;

    public Long getMemberId() {
        return memberId;
    }

    public Long getPerformanceId() {
        return performanceId;
    }

    public List<Long> getSeatIds() {
        return seatIds;
    }

    public AddReservation toAddReservation() {
        return new AddReservation(memberId, performanceId, seatIds);
    }
}
