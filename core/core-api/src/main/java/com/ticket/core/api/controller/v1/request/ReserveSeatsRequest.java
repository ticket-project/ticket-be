package com.ticket.core.api.controller.v1.request;

import com.ticket.core.domain.member.Member;
import com.ticket.core.domain.performanceseat.NewPerformanceSeats;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public class ReserveSeatsRequest {
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

    public NewPerformanceSeats toNewPerformanceSeats(final Member member) {
        return new NewPerformanceSeats(member.getId(), performanceId, seatIds);
    }
}
