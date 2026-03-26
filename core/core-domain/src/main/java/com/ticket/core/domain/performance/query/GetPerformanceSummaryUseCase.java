package com.ticket.core.domain.performance.query;

import com.ticket.core.domain.performance.model.Performance;
import com.ticket.core.domain.show.model.Show;
import com.ticket.core.domain.show.meta.Region;
import com.ticket.core.domain.show.venue.Venue;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetPerformanceSummaryUseCase {

    private final PerformanceFinder performanceFinder;

    public record Input(Long performanceId) {}

    public record Output(
            String title,
            String region,
            LocalDateTime startTime
    ) {}

    public Output execute(final Input input) {
        final Performance performance = performanceFinder.findById(input.performanceId());

        final Show show = performance.getShow();
        if (show == null) {
            throw new CoreException(
                    ErrorType.NOT_FOUND_DATA,
                    "회차에 연결된 공연을 찾을 수 없습니다. id=" + input.performanceId()
            );
        }

        final Venue venue = show.getVenue();
        final Region region = venue != null ? venue.getRegion() : null;
        return new Output(
                show.getTitle(),
                region != null ? region.getDescription() : null,
                performance.getStartTime()
        );
    }
}
