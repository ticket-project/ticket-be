package com.ticket.core.domain.performanceseat.query;

import com.ticket.core.domain.hold.command.HoldManager;
import com.ticket.core.domain.performance.model.Performance;
import com.ticket.core.domain.performance.query.PerformanceFinder;
import com.ticket.core.domain.performanceseat.command.SeatSelectionService;
import com.ticket.core.domain.performanceseat.query.model.SeatStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 좌석 상태 통합 조회 UseCase.
 * DB(AVAILABLE/RESERVED) + Redis(SELECTING/HOLDING)를 합산하여
 * AVAILABLE / OCCUPIED 두 가지 상태로 반환합니다.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetSeatStatusUseCase {

    private final PerformanceFinder performanceFinder;
    private final SeatMapQueryRepository seatMapQueryRepository;
    private final SeatSelectionService seatSelectionService;
    private final HoldManager holdManager;

    public record Input(Long performanceId) {}

    public record Output(
            List<SeatState> seats
    ) {}

    public record SeatState(Long seatId, SeatStatus status) {}

    public Output execute(Input input) {
        final Performance performance = performanceFinder.findById(input.performanceId());
        final Long perfId = performance.getId();

        final List<SeatState> dbStates = seatMapQueryRepository.findSeatStatuses(perfId);

        final Set<Long> redisOccupiedIds = mergeRedisOccupiedIds(perfId);
        if (redisOccupiedIds.isEmpty()) {
            return new Output(dbStates);
        }

        final List<SeatState> merged = dbStates.stream()
                .map(seat -> seat.status() == SeatStatus.AVAILABLE && redisOccupiedIds.contains(seat.seatId())
                        ? new SeatState(seat.seatId(), SeatStatus.OCCUPIED)
                        : seat)
                .toList();

        return new Output(merged);
    }

    private Set<Long> mergeRedisOccupiedIds(final Long performanceId) {
        final Set<Long> ids = new HashSet<>(seatSelectionService.getSelectingSeatIds(performanceId));
        ids.addAll(holdManager.getHoldingSeatIds(performanceId));
        return ids;
    }
}
