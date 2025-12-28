package com.ticket.core.domain.reservation;

import com.ticket.core.domain.member.Member;
import com.ticket.core.domain.member.MemberFinder;
import com.ticket.core.domain.performance.PerformanceFinder;
import com.ticket.core.enums.PerformanceSeatState;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import com.ticket.storage.db.core.PerformanceEntity;
import com.ticket.storage.db.core.PerformanceSeatEntity;
import com.ticket.storage.db.core.PerformanceSeatRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

//@Service
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
        final PerformanceEntity foundPerformance = performanceFinder.findOpenPerformance(newReservation.getPerformanceId());
        int updateRows = performanceSeatRepository.updateState(
                foundPerformance.getId(),
                newReservation.getSeatIds(),
                PerformanceSeatState.AVAILABLE,
                PerformanceSeatState.RESERVED
        );
        if (updateRows != newReservation.getSeatIds().size()) {
            throw new CoreException(ErrorType.SEAT_COUNT_MISMATCH);
        }
        final List<PerformanceSeatEntity> reservedSeats = performanceSeatRepository.findAllByPerformanceIdAndSeatIdIn(foundPerformance.getId(), newReservation.getSeatIds());
        reservationManager.addWithoutReserve(foundMember.getId(), foundPerformance.getId(), reservedSeats);
    }

}
