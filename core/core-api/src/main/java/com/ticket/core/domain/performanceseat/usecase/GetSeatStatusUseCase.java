package com.ticket.core.domain.performanceseat.usecase;

import com.ticket.core.api.controller.response.SeatStatusResponse;
import com.ticket.core.domain.hold.HoldRedisService;
import com.ticket.core.domain.performance.Performance;
import com.ticket.core.domain.performance.PerformanceFinder;
import com.ticket.core.domain.performanceseat.SeatMapQueryRepository;
import com.ticket.core.domain.performanceseat.SeatSelectionService;
import com.ticket.core.enums.SeatStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 좌석 상태 통합 조회 UseCase.
 * DB(AVAILABLE/RESERVED) + Redis(SELECTING/HOLDING)을 합산하여
 * AVAILABLE / OCCUPIED 두 가지 상태로 반환합니다.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetSeatStatusUseCase {

    private final PerformanceFinder performanceFinder;
    private final SeatMapQueryRepository seatMapQueryRepository;
    private final SeatSelectionService seatSelectionService;
    private final HoldRedisService holdRedisService;

    public record Input(Long performanceId) {}
    public record Output(SeatStatusResponse status) {}

    public Output execute(Input input) {
        final Performance performance = performanceFinder.findById(input.performanceId());
        final Long perfId = performance.getId();

        // 1. DB 조회 (RESERVED → OCCUPIED, 나머지 → AVAILABLE)
        final List<SeatStatusResponse.SeatState> dbStates = seatMapQueryRepository.findSeatStatuses(perfId);

        // 2. Redis SELECTING + HOLDING seatId 합산
        final Set<Long> redisOccupiedIds = mergeRedisOccupiedIds(perfId);
        if (redisOccupiedIds.isEmpty()) {
            return new Output(new SeatStatusResponse(dbStates));
        }

        // 3. DB AVAILABLE 중 Redis에 있으면 OCCUPIED로 오버라이드
        final List<SeatStatusResponse.SeatState> merged = dbStates.stream()
                .map(seat -> seat.status() == SeatStatus.AVAILABLE && redisOccupiedIds.contains(seat.seatId())
                        ? new SeatStatusResponse.SeatState(seat.seatId(), SeatStatus.OCCUPIED)
                        : seat)
                .toList();

        return new Output(new SeatStatusResponse(merged));
    }

    private Set<Long> mergeRedisOccupiedIds(Long performanceId) {
        final Set<Long> ids = new HashSet<>(seatSelectionService.getSelectingSeatIds(performanceId));
        ids.addAll(holdRedisService.getHoldingSeatIds(performanceId));
        return ids;
    }
}
