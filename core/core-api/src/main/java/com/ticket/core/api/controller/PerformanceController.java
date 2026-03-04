package com.ticket.core.api.controller;

import com.ticket.core.api.controller.docs.PerformanceControllerDocs;
import com.ticket.core.api.controller.response.PerformanceScheduleListResponse;
import com.ticket.core.api.controller.response.PerformanceSummaryResponse;
import com.ticket.core.api.controller.response.SeatAvailabilityResponse;
import com.ticket.core.domain.performance.usecase.GetPerformanceScheduleListUseCase;
import com.ticket.core.domain.performance.usecase.GetPerformanceSummaryUseCase;
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
    private final GetPerformanceSummaryUseCase getPerformanceSummaryUseCase;
    private final GetPerformanceScheduleListUseCase getPerformanceScheduleListUseCase;

    @Override
    @GetMapping("/{performanceId}/summary")
    public ApiResponse<PerformanceSummaryResponse> getPerformanceSummary(
            @PathVariable final Long performanceId
    ) {
        final GetPerformanceSummaryUseCase.Input input = new GetPerformanceSummaryUseCase.Input(performanceId);
        final GetPerformanceSummaryUseCase.Output output = getPerformanceSummaryUseCase.execute(input);
        return ApiResponse.success(output.summary());
    }

    @Override
    @GetMapping("/{performanceId}/schedules")
    public ApiResponse<PerformanceScheduleListResponse> getPerformanceSchedules(
            @PathVariable final Long performanceId
    ) {
        final GetPerformanceScheduleListUseCase.Input input = new GetPerformanceScheduleListUseCase.Input(performanceId);
        final GetPerformanceScheduleListUseCase.Output output = getPerformanceScheduleListUseCase.execute(input);
        return ApiResponse.success(output.schedules());
    }

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
