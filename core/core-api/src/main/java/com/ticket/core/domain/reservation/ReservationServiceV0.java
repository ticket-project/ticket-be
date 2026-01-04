package com.ticket.core.domain.reservation;

import com.ticket.core.domain.member.Member;
import com.ticket.core.domain.member.MemberFinder;
import com.ticket.core.domain.performance.Performance;
import com.ticket.core.domain.performance.PerformanceFinder;
import com.ticket.core.domain.performanceseat.PerformanceSeat;
import com.ticket.core.domain.performanceseat.PerformanceSeatRepository;
import com.ticket.core.enums.PerformanceSeatState;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * DB 행 락을 통한 예매 서비스 (동시성 문제 방지)
 */
@Service("reservationServiceV0")
public class ReservationServiceV0 implements ReservationService {

    private final MemberFinder memberFinder;
    private final PerformanceFinder performanceFinder;
    private final PerformanceSeatRepository performanceSeatRepository;
    private final ReservationManager reservationManager;

    public ReservationServiceV0(final MemberFinder memberFinder,
                                final PerformanceFinder performanceFinder,
                                final PerformanceSeatRepository performanceSeatRepository,
                                final ReservationManager reservationManager
) {
        this.memberFinder = memberFinder;
        this.performanceFinder = performanceFinder;
        this.performanceSeatRepository = performanceSeatRepository;
        this.reservationManager = reservationManager;
    }

    @Transactional
    public void addReservation(final NewReservation newReservation) {
        final Member foundMember = memberFinder.find(newReservation.getMemberId());
        final Performance foundPerformance = performanceFinder.findOpenPerformance(newReservation.getPerformanceId());
        int updateRows = performanceSeatRepository.updateState(
                foundPerformance.getId(),
                newReservation.getSeatIds(),
                PerformanceSeatState.AVAILABLE,
                PerformanceSeatState.RESERVED
        );
        if (updateRows != newReservation.getSeatIds().size()) {
            throw new CoreException(ErrorType.SEAT_COUNT_MISMATCH);
        }
        final List<PerformanceSeat> reservedSeats = performanceSeatRepository.findAllByPerformanceIdAndSeatIdIn(foundPerformance.getId(), newReservation.getSeatIds());
        reservationManager.addWithoutReserve(foundMember.getId(), foundPerformance.getId(), reservedSeats);
    }

}
