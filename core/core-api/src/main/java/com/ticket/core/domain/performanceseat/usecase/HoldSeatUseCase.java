package com.ticket.core.domain.performanceseat.usecase;

import com.ticket.core.domain.performance.Performance;
import com.ticket.core.domain.performance.PerformanceFinder;
import com.ticket.core.domain.performanceseat.*;
import com.ticket.core.domain.performanceseat.SeatStatusMessage.SeatAction;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 좌석 선점(Hold) UseCase.
 * DB AVAILABLE 상태의 좌석을 Redis Hold로 전이시킵니다.
 * Select 없이도 바로 Hold가 가능합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HoldSeatUseCase {

    private final PerformanceFinder performanceFinder;
    private final PerformanceSeatFinder performanceSeatFinder;
    private final SeatSelectionService seatSelectionService;
    private final SeatHoldService seatHoldService;
    private final SeatEventPublisher seatEventPublisher;

    public record Input(Long performanceId, List<Long> seatIds, Long memberId) {}

    @Transactional(readOnly = true)
    public void execute(final Input input) {
        final Performance performance = performanceFinder.findById(input.performanceId());

        // 1. 예매 오픈 시간 검증
        if (!performance.isBookingOpen(LocalDateTime.now())) {
            throw new CoreException(ErrorType.NOT_YET_RESERVE_TIME);
        }

        // 2. 최대 선점 가능 좌석 수 검증
        if (performance.isOverCount(input.seatIds().size())) {
            throw new CoreException(ErrorType.EXCEED_AVAILABLE_SEATS);
        }

        // 3. DB에서 AVAILABLE 상태 확인 (비관적 락)
        final List<PerformanceSeat> availableSeats = performanceSeatFinder
                .findAvailablePerformanceSeats(input.seatIds(), input.performanceId());
        if (availableSeats.size() != input.seatIds().size()) {
            throw new CoreException(ErrorType.SEAT_COUNT_MISMATCH);
        }

        // 4. Redis Hold (Lua 스크립트 원자적 처리)
        seatHoldService.hold(
                input.performanceId(),
                input.seatIds(),
                input.memberId(),
                performance.getHoldTime()
        );

        // 5. Select 키가 존재하면 정리 (Select 없이 Hold한 경우 무시)
        for (final Long seatId : input.seatIds()) {
            seatSelectionService.forceDeselectIfExists(input.performanceId(), seatId);
        }

        // 6. WebSocket HELD 이벤트 발행
        for (final Long seatId : input.seatIds()) {
            seatEventPublisher.publish(SeatStatusMessage.of(input.performanceId(), seatId, SeatAction.HELD));
        }

        log.info("좌석 선점 완료: performanceId={}, seatIds={}, memberId={}, holdTime={}s",
                input.performanceId(), input.seatIds(), input.memberId(), performance.getHoldTime());
    }
}
