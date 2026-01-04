package com.ticket.core.domain.reservation;

import com.ticket.core.domain.member.Member;
import com.ticket.core.domain.member.MemberFinder;
import com.ticket.core.domain.performance.Performance;
import com.ticket.core.domain.performance.PerformanceFinder;
import com.ticket.core.domain.performanceseat.PerformanceSeat;
import com.ticket.core.domain.performanceseat.PerformanceSeatFinder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 비관적 락을 통한 예매 서비스 (동시성 문제 방지)
 */
@Service("reservationServiceV1")
public class ReservationServiceV1 implements ReservationService {

    private final MemberFinder memberFinder;
    private final PerformanceFinder performanceFinder;
    private final PerformanceSeatFinder performanceSeatFinder;
    private final ReservationValidator reservationValidator;
    private final ReservationManager reservationManager;

    public ReservationServiceV1(final MemberFinder memberFinder,
                                final PerformanceFinder performanceFinder,
                                final PerformanceSeatFinder performanceSeatFinder,
                                final ReservationValidator reservationValidator,
                                final ReservationManager reservationManager
) {
        this.memberFinder = memberFinder;
        this.performanceFinder = performanceFinder;
        this.performanceSeatFinder = performanceSeatFinder;
        this.reservationValidator = reservationValidator;
        this.reservationManager = reservationManager;
    }

    @Transactional
    public void addReservation(final NewReservation newReservation) {
        final Member foundMember = memberFinder.find(newReservation.getMemberId());
        final Performance foundPerformance = performanceFinder.findOpenPerformance(newReservation.getPerformanceId());
        final List<PerformanceSeat> foundPerformanceSeats = performanceSeatFinder.findAvailablePerformanceSeats(
                newReservation.getSeatIds(),
                foundPerformance.getId()
        );
        //여기서 가격 계산, 쿠폰,포인트 등 추가 예정
        reservationValidator.validateNew(
                newReservation.getSeatIds().size(),
                foundMember.getId(),
                foundPerformance,
                foundPerformanceSeats.size()
        );
        reservationManager.add(foundMember.getId(), foundPerformance.getId(), foundPerformanceSeats);
    }

}
