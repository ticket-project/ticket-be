package com.ticket.core.api.controller.request;

import com.ticket.core.domain.reservation.NewReservation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

@Schema(description = "예약 요청")
public class AddReservationRequest {

    @Schema(description = "공연 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @Positive
    @NotNull
    private Long performanceId;

    @Schema(description = "예약할 좌석 ID 목록", example = "[1, 2, 3]", requiredMode = Schema.RequiredMode.REQUIRED)
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

