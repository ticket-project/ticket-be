package com.ticket.core.domain.performance.usecase;

import com.ticket.core.api.controller.response.PerformanceScheduleListResponse;
import com.ticket.core.api.controller.response.PerformanceScheduleListResponse.PerformanceScheduleItem;
import com.ticket.core.domain.performance.Performance;
import com.ticket.core.domain.performance.PerformanceFinder;
import com.ticket.core.domain.performance.PerformanceRepository;
import com.ticket.core.domain.show.Show;
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
public class GetPerformanceScheduleListUseCase {

    private final PerformanceFinder performanceFinder;
    private final PerformanceRepository performanceRepository;

    public record Input(Long performanceId) {
    }

    public record Output(PerformanceScheduleListResponse schedules) {
    }

    public Output execute(final Input input) {
        final Performance findPerformance = performanceFinder.findActiveById(input.performanceId());

        final Show show = findPerformance.getShow();
        if (show == null) {
            throw new CoreException(
                    ErrorType.NOT_FOUND_DATA,
                    "회차에 연결된 공연을 찾을 수 없습니다. id=" + input.performanceId()
            );
        }

        final List<PerformanceScheduleItem> scheduleItems = performanceRepository
                .findAllByShowIdAndStatusOrderByStartTimeAscPerformanceNoAsc(show.getId(), EntityStatus.ACTIVE)
                .stream()
                .map(performance -> new PerformanceScheduleItem(
                        performance.getId(),
                        performance.getPerformanceNo(),
                        performance.getStartTime(),
                        performance.getState()
                ))
                .toList();

        final PerformanceScheduleListResponse response = new PerformanceScheduleListResponse(
                show.getId(),
                findPerformance.getId(),
                scheduleItems
        );
        return new Output(response);
    }
}
