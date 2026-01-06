package com.ticket.core.domain.hold;

import com.ticket.core.aop.DistributedLock;
import com.ticket.core.domain.member.Member;
import com.ticket.core.domain.member.MemberFinder;
import com.ticket.core.domain.performance.Performance;
import com.ticket.core.domain.performance.PerformanceFinder;
import com.ticket.core.domain.performanceseat.PerformanceSeat;
import com.ticket.core.domain.performanceseat.PerformanceSeatFinder;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Redisson 분산락을 통한 좌석 선점 서비스 (동시성 문제 방지)
 */
@Service
public class HoldServiceV1 {
    private final MemberFinder memberFinder;
    private final PerformanceFinder performanceFinder;
    private final PerformanceSeatFinder performanceSeatFinder;

    public HoldServiceV1(final MemberFinder memberFinder,
                         final PerformanceFinder performanceFinder,
                         final PerformanceSeatFinder performanceSeatFinder
    ) {
        this.memberFinder = memberFinder;
        this.performanceFinder = performanceFinder;
        this.performanceSeatFinder = performanceSeatFinder;
    }

    @DistributedLock(prefix = "SEAT", dynamicKey = "#newSeatHold.getSeatIds()")
    public HoldInfo hold(final NewSeatHold newSeatHold) {
        final Member foundMember = memberFinder.find(newSeatHold.getMemberId());
        final Performance foundPerformance = performanceFinder.findOpenPerformance(newSeatHold.getPerformanceId());
        final List<PerformanceSeat> availablePerformanceSeats = performanceSeatFinder.findAvailablePerformanceSeats(newSeatHold.getSeatIds(), foundPerformance.getId());
        if (availablePerformanceSeats.isEmpty()) {
            throw new CoreException(ErrorType.NOT_FOUND_DATA, "가능한 좌석이 없습니다.");
        }
        if (availablePerformanceSeats.size() != newSeatHold.getSeatIds().size()) {
            throw new CoreException(ErrorType.SEAT_COUNT_MISMATCH);
        }
        final LocalDateTime holdExpireAt = LocalDateTime.now().plusSeconds(foundPerformance.getHoldTime());
        availablePerformanceSeats.forEach(performanceSeat -> performanceSeat.hold(foundMember.getId(), holdExpireAt));
        return new HoldInfo(foundMember.getId(),
                foundPerformance.getId(),
                availablePerformanceSeats.stream().map(PerformanceSeat::getSeatId).toList(),
                holdExpireAt
        );
    }

}
