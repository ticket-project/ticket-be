package com.ticket.core.domain.performance;

import com.ticket.core.enums.EntityStatus;
import com.ticket.core.enums.PerformanceState;
import com.ticket.core.support.exception.ErrorType;
import com.ticket.core.support.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PerformanceFinder {

    private final PerformanceRepository performanceRepository;

    public Performance findOpenPerformance(final Long performanceId) {
        return performanceRepository.findByIdAndStateAndStatus(
                        performanceId,
                        PerformanceState.OPEN,
                        EntityStatus.ACTIVE
                )
                .orElseThrow(() -> new NotFoundException(ErrorType.NOT_FOUND_DATA));
    }

}
