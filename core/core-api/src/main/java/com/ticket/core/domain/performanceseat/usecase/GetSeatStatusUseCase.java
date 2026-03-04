package com.ticket.core.domain.performanceseat.usecase;

import com.ticket.core.api.controller.response.SeatStatusResponse;
import com.ticket.core.domain.performance.Performance;
import com.ticket.core.domain.performance.PerformanceFinder;
import com.ticket.core.domain.performanceseat.SeatMapQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetSeatStatusUseCase {

    private final PerformanceFinder performanceFinder;
    private final SeatMapQueryRepository seatMapQueryRepository;

    public record Input(Long performanceId) {}
    public record Output(SeatStatusResponse status) {}

    public Output execute(Input input) {
        final Performance performance = performanceFinder.findActivePerformancesById(input.performanceId());

        List<SeatStatusResponse.SeatState> seatStates =
                seatMapQueryRepository.findSeatStatuses(performance.getId());

        return new Output(new SeatStatusResponse(seatStates));
    }
}
