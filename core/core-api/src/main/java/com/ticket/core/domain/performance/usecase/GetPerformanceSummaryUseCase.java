package com.ticket.core.domain.performance.usecase;

import com.ticket.core.domain.performance.Performance;
import com.ticket.core.domain.performance.PerformanceFinder;
import com.ticket.core.domain.show.Show;
import com.ticket.core.domain.show.meta.Region;
import com.ticket.core.domain.show.venue.Venue;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import io.swagger.v3.oas.annotations.media.Schema;
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
            @Schema(description = "공연 제목", example = "싱어게인4 전국투어 콘서트 - 대전") String title,
            @Schema(description = "지역", example = "충청") String region,
            @Schema(description = "공연 일시", example = "2026-04-04T14:00:00") LocalDateTime startTime
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
