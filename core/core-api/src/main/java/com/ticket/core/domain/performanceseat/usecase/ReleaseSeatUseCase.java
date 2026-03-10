package com.ticket.core.domain.performanceseat.usecase;

import com.ticket.core.domain.performanceseat.SeatEventPublisher;
import com.ticket.core.domain.performanceseat.SeatHoldService;
import com.ticket.core.domain.performanceseat.SeatStatusMessage;
import com.ticket.core.domain.performanceseat.SeatStatusMessage.SeatAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 좌석 선점 해제 UseCase.
 * 본인이 Hold한 좌석을 해제합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReleaseSeatUseCase {

    private final SeatHoldService seatHoldService;
    private final SeatEventPublisher seatEventPublisher;

    public record Input(Long performanceId, List<Long> seatIds, Long memberId) {}

    public void execute(final Input input) {
        seatHoldService.release(input.performanceId(), input.seatIds(), input.memberId());

        for (final Long seatId : input.seatIds()) {
            seatEventPublisher.publish(SeatStatusMessage.of(input.performanceId(), seatId, SeatAction.RELEASED));
        }

        log.info("좌석 선점 해제 완료: performanceId={}, seatIds={}, memberId={}",
                input.performanceId(), input.seatIds(), input.memberId());
    }
}
