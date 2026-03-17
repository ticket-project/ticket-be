package com.ticket.core.domain.performance.usecase;

import com.ticket.core.api.controller.response.PerformanceScheduleListResponse.PerformanceScheduleItem;
import com.ticket.core.domain.performance.Performance;
import com.ticket.core.domain.performance.PerformanceFinder;
import com.ticket.core.domain.performance.PerformanceRepository;
import com.ticket.core.domain.show.Show;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetPerformanceScheduleListUseCase {

    private final PerformanceFinder performanceFinder;
    private final PerformanceRepository performanceRepository;

    public record Input(Long performanceId) {}

    public record Output(
            @Schema(description = "공연 ID", example = "1") Long showId,
            @Schema(description = "현재 선택된 회차 ID", example = "101") Long selectedPerformanceId,
            @Schema(description = "같은 공연의 회차 목록") List<PerformanceScheduleItem> schedules
    ) {}

    public Output execute(final Input input) {
        final Performance findPerformance = performanceFinder.findById(input.performanceId());

        final Show show = findPerformance.getShow();
        if (show == null) {
            throw new CoreException(
                    ErrorType.NOT_FOUND_DATA,
                    "회차에 연결된 공연을 찾을 수 없습니다. id=" + input.performanceId()
            );
        }

        final List<PerformanceScheduleItem> scheduleItems = performanceRepository
                .findAllByShowIdOrderByStartTimeAscPerformanceNoAsc(show.getId())
                .stream()
                .map(performance -> new PerformanceScheduleItem(
                        performance.getId(),
                        performance.getPerformanceNo(),
                        performance.getStartTime()
                ))
                .toList();

        return new Output(show.getId(), findPerformance.getId(), scheduleItems);
    }
}
