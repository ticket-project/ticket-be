package com.ticket.core.api.controller;

import com.ticket.core.api.controller.response.SeatAvailabilityResponse;
import com.ticket.core.domain.performanceseat.usecase.GetSeatAvailabilityUseCase;
import com.ticket.core.support.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/performances")
@Tag(name = "공연 회차(Performance)", description = "공연 회차 관련 API")
public class PerformanceController {

    private final GetSeatAvailabilityUseCase getSeatAvailabilityUseCase;

    public PerformanceController(GetSeatAvailabilityUseCase getSeatAvailabilityUseCase) {
        this.getSeatAvailabilityUseCase = getSeatAvailabilityUseCase;
    }

    @GetMapping("/{performanceId}/seats/availability")
    @Operation(
            summary = "회차별 등급별 잔여석 조회",
            description = """
                    특정 공연 회차의 등급별 잔여 좌석 수를 조회합니다.
                    등급 코드, 등급명, 가격, 총 좌석 수, 잔여 좌석 수를 반환합니다.
                    """
    )
    public ApiResponse<SeatAvailabilityResponse> getSeatAvailability(
            @Parameter(description = "공연 회차 ID", example = "1", required = true)
            @PathVariable final Long performanceId
    ) {
        final GetSeatAvailabilityUseCase.Input input = new GetSeatAvailabilityUseCase.Input(performanceId);
        final GetSeatAvailabilityUseCase.Output output = getSeatAvailabilityUseCase.execute(input);
        return ApiResponse.success(output.availability());
    }
}
