package com.ticket.core.domain.performanceseat.command;

import com.ticket.core.domain.performanceseat.store.SeatSelectionStore;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeatSelectionService {

    private static final Duration SELECT_TTL = Duration.ofMinutes(5);

    private final SeatSelectionStore seatSelectionStore;

    public void select(final Long performanceId, final Long seatId, final Long memberId) {
        final boolean locked = seatSelectionStore.selectIfAbsent(performanceId, seatId, memberId.toString(), SELECT_TTL);
        if (!locked) {
            log.warn("좌석 선택에 실패했습니다. performanceId={}, seatId={}, memberId={}", performanceId, seatId, memberId);
            throw new CoreException(ErrorType.SEAT_ALREADY_SELECTED);
        }

        log.info("좌석 선택에 성공했습니다. performanceId={}, seatId={}, memberId={}", performanceId, seatId, memberId);
    }

    public void deselect(final Long performanceId, final Long seatId, final Long memberId) {
        final String memberKey = memberId.toString();

        final String holder = seatSelectionStore.getHolder(performanceId, seatId);
        if (holder == null) {
            log.info("좌석 선택 해제를 건너뜁니다. 이미 선택 정보가 없습니다. performanceId={}, seatId={}", performanceId, seatId);
            return;
        }

        if (!memberKey.equals(holder)) {
            log.warn("좌석 선택 해제 권한이 없습니다. performanceId={}, seatId={}, requestMemberId={}, holderMemberId={}",
                    performanceId, seatId, memberId, holder);
            throw new CoreException(ErrorType.SEAT_NOT_OWNED);
        }

        final boolean released = seatSelectionStore.releaseIfOwned(performanceId, seatId, memberKey);
        if (!released) {
            final String currentHolder = seatSelectionStore.getHolder(performanceId, seatId);
            if (currentHolder == null) {
                log.info("좌석 선택 해제 시점에 이미 만료되었거나 해제되었습니다. performanceId={}, seatId={}, memberId={}",
                        performanceId, seatId, memberId);
                return;
            }

            log.warn("좌석 선택 해제 권한이 없습니다. performanceId={}, seatId={}, requestMemberId={}, holderMemberId={}",
                    performanceId, seatId, memberId, currentHolder);
            throw new CoreException(ErrorType.SEAT_NOT_OWNED);
        }
        log.info("좌석 선택 해제에 성공했습니다. performanceId={}, seatId={}, memberId={}", performanceId, seatId, memberId);
    }

    public DeselectedSeatIds deselectAll(final Long performanceId, final Long memberId) {
        final String memberKey = memberId.toString();
        final List<Long> deselectedSeatIds = seatSelectionStore.releaseAllByMember(performanceId, memberKey);
        logDeselectedSeats(performanceId, memberId, deselectedSeatIds);
        return DeselectedSeatIds.from(deselectedSeatIds);
    }

    public void forceDeselect(final Long performanceId, final Long seatId) {
        seatSelectionStore.forceRelease(performanceId, seatId);
    }

    public java.util.Set<Long> getSelectingSeatIds(final Long performanceId) {
        return seatSelectionStore.getSelectingSeatIds(performanceId);
    }

    private void logDeselectedSeats(final Long performanceId, final Long memberId, final List<Long> seatIds) {
        for (final Long seatId : seatIds) {
            log.info("좌석 일괄 선택 해제에 성공했습니다. performanceId={}, seatId={}, memberId={}", performanceId, seatId, memberId);
        }
    }
}
