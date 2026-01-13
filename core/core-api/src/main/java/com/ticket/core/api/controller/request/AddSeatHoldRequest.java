package com.ticket.core.api.controller.request;

import com.ticket.core.domain.hold.NewSeatHold;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public class AddSeatHoldRequest {

    @Positive
    @NotNull
    private Long performanceId;

    @NotEmpty
    private List<@Positive @NotNull Long> seatIds;

    public Long getPerformanceId() {
        return performanceId;
    }

    public List<Long> getSeatIds() {
        return seatIds;
    }

    public NewSeatHold toNewSeatHold() {
        return new NewSeatHold(performanceId, seatIds);
    }
}
