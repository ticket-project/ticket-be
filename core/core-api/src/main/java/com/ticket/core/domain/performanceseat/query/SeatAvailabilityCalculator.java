package com.ticket.core.domain.performanceseat.query;

import com.ticket.core.api.controller.response.SeatAvailabilityResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class SeatAvailabilityCalculator {

    public SeatAvailabilityResponse calculate(
            final List<AvailableSeatRow> rows,
            final Set<Long> redisOccupiedSeatIds
    ) {
        if (rows.isEmpty()) {
            return new SeatAvailabilityResponse(List.of());
        }

        final Map<GradeKey, Long> availableSeatCounts = new LinkedHashMap<>();
        for (final AvailableSeatRow row : rows) {
            final GradeKey gradeKey = new GradeKey(row.gradeName(), row.sortOrder());
            availableSeatCounts.putIfAbsent(gradeKey, 0L);

            if (!redisOccupiedSeatIds.contains(row.seatId())) {
                availableSeatCounts.computeIfPresent(gradeKey, (key, count) -> count + 1L);
            }
        }

        final List<SeatAvailabilityResponse.GradeAvailability> grades = new ArrayList<>(availableSeatCounts.size());
        for (final Map.Entry<GradeKey, Long> entry : availableSeatCounts.entrySet()) {
            grades.add(new SeatAvailabilityResponse.GradeAvailability(
                    entry.getKey().gradeName(),
                    entry.getKey().sortOrder(),
                    entry.getValue()
            ));
        }

        return new SeatAvailabilityResponse(grades);
    }

    public record AvailableSeatRow(Long seatId, String gradeName, int sortOrder) {
    }

    private record GradeKey(String gradeName, int sortOrder) {
    }
}
