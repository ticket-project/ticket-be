package com.ticket.core.domain.performance.usecase;

import com.ticket.core.api.controller.response.PerformanceSummaryResponse;
import com.ticket.core.domain.performance.Performance;
import com.ticket.core.domain.performance.PerformanceFinder;
import com.ticket.core.domain.performance.PerformanceRepository;
import com.ticket.core.domain.show.Region;
import com.ticket.core.domain.show.Show;
import com.ticket.core.domain.show.Venue;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetPerformanceSummaryUseCase {

    private final PerformanceRepository performanceRepository;
    private final PerformanceFinder performanceFinder;

    public record Input(Long performanceId) {
    }

    public record Output(PerformanceSummaryResponse summary) {
    }

    public Output execute(final Input input) {
        final Performance performance = performanceFinder.findActivePerformancesById(input.performanceId());

        final Show show = performance.getShow();
        if (show == null) {
            throw new CoreException(
                    ErrorType.NOT_FOUND_DATA,
                    "회차에 연결된 공연을 찾을 수 없습니다. id=" + input.performanceId()
            );
        }

        final Venue venue = show.getVenue();
        final Region region = venue != null ? venue.getRegion() : null;
        final PerformanceSummaryResponse response = new PerformanceSummaryResponse(
                show.getTitle(),
                region != null ? region.getDescription() : null,
                performance.getStartTime()
        );
        return new Output(response);
    }
}
