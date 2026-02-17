package com.ticket.core.domain.hold;

import com.ticket.core.aop.DistributedLock;
import com.ticket.core.domain.member.Member;
import com.ticket.core.domain.member.MemberFinder;
import com.ticket.core.domain.performance.Performance;
import com.ticket.core.domain.performance.PerformanceFinder;
import com.ticket.core.domain.performanceseat.PerformanceSeat;
import com.ticket.core.domain.performanceseat.PerformanceSeatFinder;
import com.ticket.core.domain.seat.Seat;
import com.ticket.core.domain.seat.SeatRepository;
import com.ticket.core.enums.HoldState;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * v1: Redisson 분산락을 통한 좌석 선점 서비스 (동시성 문제 방지)
 */
//@Service
@RequiredArgsConstructor
public class HoldLockService implements HoldService {
    private final MemberFinder memberFinder;
    private final PerformanceFinder performanceFinder;
    private final PerformanceSeatFinder performanceSeatFinder;
    private final HoldRepository holdRepository;
    private final HoldItemRepository holdItemRepository;
    private final SeatRepository seatRepository;

    @DistributedLock(prefix = "SEAT", dynamicKey = "#newHold.getSeatIds()")
    public Long hold(final Long memberId, final NewHold newHold) {
        final Member foundMember = memberFinder.find(memberId);
        final Performance foundPerformance = performanceFinder.findOpenPerformance(newHold.getPerformanceId());
        final List<Seat> foundSeats = seatRepository.findByIdIn(newHold.getSeatIds());
        final List<PerformanceSeat> availablePerformanceSeats = performanceSeatFinder.findAllByPerformanceAndSeatIn(foundPerformance, foundSeats);
        validatePerformanceSeats(newHold, availablePerformanceSeats);
        availablePerformanceSeats.forEach(PerformanceSeat::hold);

        final Hold savedHold = saveHold(foundMember, foundPerformance);
        saveHoldItems(availablePerformanceSeats, savedHold);
        return savedHold.getId();
    }

    private void validatePerformanceSeats(final NewHold newHold, final List<PerformanceSeat> availablePerformanceSeats) {
        if (availablePerformanceSeats.isEmpty()) {
            throw new CoreException(ErrorType.NOT_FOUND_DATA, "가능한 좌석이 없습니다.");
        }
        if (availablePerformanceSeats.size() != newHold.getSeatIds().size()) {
            throw new CoreException(ErrorType.SEAT_COUNT_MISMATCH);
        }
    }

    private Hold saveHold(final Member member, final Performance performance) {
        final LocalDateTime holdExpireAt = LocalDateTime.now().plusSeconds(performance.getHoldTime());
        final Hold hold = new Hold(member, holdExpireAt, HoldState.ACTIVE);
        return holdRepository.save(hold);
    }

    private void saveHoldItems(final List<PerformanceSeat> availablePerformanceSeats, final Hold savedHold) {
        List<HoldItem> holdItems = availablePerformanceSeats.stream()
                .map(availablePerformanceSeat -> new HoldItem(
                        savedHold, availablePerformanceSeat, availablePerformanceSeat.getPrice()))
                .toList();
        holdItemRepository.saveAll(holdItems);
    }

}
