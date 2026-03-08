package com.ticket.core.domain.performance;

import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import com.ticket.core.support.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class PerformanceFinder {

    private final PerformanceRepository performanceRepository;

    public Performance findOpenPerformance(final Long performanceId) {
        final Performance performance = findActiveById(performanceId);
        if (!performance.isBookingOpen(LocalDateTime.now())) {
            throw new NotFoundException(ErrorType.NOT_FOUND_DATA);
        }
        return performance;
    }

    public Performance findActiveById(final Long performanceId) {
        return performanceRepository.findById(performanceId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA, "회차를 찾을 수 없습니다. id=" + performanceId));
    }
}