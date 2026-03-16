package com.ticket.core.domain.performance;

import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import com.ticket.core.support.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class PerformanceFinder {

    private final PerformanceRepository performanceRepository;
    private final Clock clock;

    public Performance findOpenPerformance(final Long performanceId) {
        final Performance performance = findById(performanceId);
        if (!performance.isBookingOpen(LocalDateTime.now(clock))) {
            throw new NotFoundException(ErrorType.NOT_FOUND_DATA);
        }
        return performance;
    }

    public Performance findById(final Long performanceId) {
        return performanceRepository.findById(performanceId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA, "회차를 찾을 수 없습니다. id=" + performanceId));
    }

    public Performance findValidPerformanceById(final Long performanceId) {
        final Performance performance = performanceRepository.findById(performanceId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA, "회차를 찾을 수 없습니다. id=" + performanceId));
        validatePerformance(performance);
        return performance;
    }

    private void validatePerformance(final Performance performance) {
        final LocalDateTime now = LocalDateTime.now(clock);
        if (performance.getOrderOpenTime() == null || now.isBefore(performance.getOrderOpenTime())) {
            throw new CoreException(ErrorType.NOT_YET_RESERVE_TIME);
        }
        if (performance.getOrderCloseTime() == null || now.isAfter(performance.getOrderCloseTime())) {
            throw new CoreException(ErrorType.PERFORMANCE_IS_PAST);
        }
    }
}
