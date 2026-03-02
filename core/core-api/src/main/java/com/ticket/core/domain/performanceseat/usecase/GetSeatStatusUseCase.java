package com.ticket.core.domain.performanceseat.usecase;

import com.ticket.core.api.controller.response.SeatStatusResponse;
import com.ticket.core.domain.performance.PerformanceRepository;
import com.ticket.core.domain.performanceseat.SeatMapQueryRepository;
import com.ticket.core.enums.EntityStatus;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetSeatStatusUseCase {

    private final PerformanceRepository performanceRepository;
    private final SeatMapQueryRepository seatMapQueryRepository;

    public record Input(Long performanceId) {}
    public record Output(SeatStatusResponse status) {}

    public Output execute(Input input) {
        performanceRepository.findByIdAndStatus(input.performanceId(), EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(
                        ErrorType.NOT_FOUND_DATA,
                        "회차를 찾을 수 없습니다. id=" + input.performanceId()
                ));

        List<SeatStatusResponse.SeatState> seatStates =
                seatMapQueryRepository.findSeatStatuses(input.performanceId());

        return new Output(new SeatStatusResponse(seatStates));
    }
}
