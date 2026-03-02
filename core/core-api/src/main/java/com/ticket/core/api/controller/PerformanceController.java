package com.ticket.core.api.controller;

import com.ticket.core.api.controller.docs.PerformanceControllerDocs;
import com.ticket.core.api.controller.response.SeatAvailabilityResponse;
import com.ticket.core.api.controller.response.SeatStatusResponse;
import com.ticket.core.domain.performanceseat.usecase.GetSeatAvailabilityUseCase;
import com.ticket.core.domain.performanceseat.usecase.GetSeatStatusUseCase;
import com.ticket.core.support.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/performances")
@RequiredArgsConstructor
public class PerformanceController implements PerformanceControllerDocs {

    private final GetSeatAvailabilityUseCase getSeatAvailabilityUseCase;
    private final GetSeatStatusUseCase getSeatStatusUseCase;

    @Override
    @GetMapping("/{performanceId}/seats/availability")
    public ApiResponse<SeatAvailabilityResponse> getSeatAvailability(
            @PathVariable final Long performanceId
    ) {
        final GetSeatAvailabilityUseCase.Input input = new GetSeatAvailabilityUseCase.Input(performanceId);
        final GetSeatAvailabilityUseCase.Output output = getSeatAvailabilityUseCase.execute(input);
        return ApiResponse.success(output.availability());
    }

    @Override
    @GetMapping("/{performanceId}/seats/status")
    public ApiResponse<SeatStatusResponse> getSeatStatus(
            @PathVariable final Long performanceId
    ) {
        final GetSeatStatusUseCase.Input input = new GetSeatStatusUseCase.Input(performanceId);
        final GetSeatStatusUseCase.Output output = getSeatStatusUseCase.execute(input);
        return ApiResponse.success(output.status());
    }
}
