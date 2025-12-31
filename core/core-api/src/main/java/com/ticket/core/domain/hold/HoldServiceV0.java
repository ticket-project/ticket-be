package com.ticket.core.domain.hold;

import com.ticket.core.domain.member.Member;
import com.ticket.core.domain.member.MemberFinder;
import com.ticket.core.domain.performance.Performance;
import com.ticket.core.domain.performance.PerformanceFinder;
import com.ticket.core.domain.performanceseat.PerformanceSeat;
import com.ticket.core.domain.performanceseat.PerformanceSeatFinder;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HoldServiceV0 implements HoldService {

    private final MemberFinder memberFinder;
    private final PerformanceFinder performanceFinder;
    private final PerformanceSeatFinder performanceSeatFinder;

    public HoldServiceV0(final MemberFinder memberFinder,
                         final PerformanceFinder performanceFinder,
                         final PerformanceSeatFinder performanceSeatFinder
    ) {
        this.memberFinder = memberFinder;
        this.performanceFinder = performanceFinder;
        this.performanceSeatFinder = performanceSeatFinder;
    }

    @Override
    @Transactional
    public HoldToken hold(final NewSeatHold newSeatHold) {
        final Member foundMember = memberFinder.find(newSeatHold.getMemberId());
        final Performance foundPerformance = performanceFinder.findOpenPerformance(newSeatHold.getPerformanceId());
        final List<PerformanceSeat> availablePerformanceSeats = performanceSeatFinder.findAvailablePerformanceSeats(newSeatHold.getSeatIds(), foundPerformance.getId());
        if (availablePerformanceSeats.isEmpty()) {
            throw new CoreException(ErrorType.NOT_FOUND_DATA, "가능한 좌석이 없습니다.");
        }
        if (availablePerformanceSeats.size() != newSeatHold.getSeatIds().size()) {
            throw new CoreException(ErrorType.SEAT_COUNT_MISMATCH);
        }
        final HoldToken holdToken = new HoldToken(foundMember.getId());
        availablePerformanceSeats.forEach(performanceSeat -> performanceSeat.hold(foundPerformance.getHoldTime(), holdToken.getToken()));
        return holdToken;
    }

}
