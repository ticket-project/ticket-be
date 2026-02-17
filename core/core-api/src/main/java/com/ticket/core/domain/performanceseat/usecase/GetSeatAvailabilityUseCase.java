package com.ticket.core.domain.performanceseat.usecase;

import com.ticket.core.api.controller.response.SeatAvailabilityResponse;
import com.ticket.core.domain.performance.Performance;
import com.ticket.core.domain.performance.PerformanceRepository;
import com.ticket.core.domain.performanceseat.SeatAvailabilityQueryRepository;
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
public class GetSeatAvailabilityUseCase {

    private final PerformanceRepository performanceRepository;
    private final SeatAvailabilityQueryRepository seatAvailabilityQueryRepository;

    public record Input(Long performanceId) {}

    public record Output(SeatAvailabilityResponse availability) {}

    public Output execute(Input input) {
        Performance performance = performanceRepository
                .findByIdAndStatus(input.performanceId(), EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(
                        ErrorType.NOT_FOUND_DATA,
                        "회차를 찾을 수 없습니다. id=" + input.performanceId()
                ));

        if (performance.getShow() == null) {
            throw new CoreException(ErrorType.NOT_FOUND_DATA,
                    "회차에 연결된 공연을 찾을 수 없습니다. id=" + input.performanceId());
        }

        SeatAvailabilityResponse response = seatAvailabilityQueryRepository
                .findSeatAvailability(performance.getId(), performance.getShow().getId());
        if (response == null) {
            response = new SeatAvailabilityResponse(List.of());
        }

        return new Output(response);
    }
}
