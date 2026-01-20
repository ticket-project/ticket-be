package com.ticket.core.api.controller.request;

import com.ticket.core.domain.hold.NewHold;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

@Schema(description = "좌석 선점 요청")
public class AddHoldRequest {

    @Schema(description = "공연 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @Positive
    @NotNull
    private Long performanceId;

    @Schema(description = "선점할 좌석 ID 목록", example = "[1, 2, 3]", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty
    private List<@Positive @NotNull Long> seatIds;

    public Long getPerformanceId() {
        return performanceId;
    }

    public List<Long> getSeatIds() {
        return seatIds;
    }

    public NewHold toNewHold() {
        return new NewHold(performanceId, seatIds);
    }
}

