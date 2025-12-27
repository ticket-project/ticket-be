package com.ticket.core.domain.reservation;

import com.ticket.core.domain.member.Member;
import com.ticket.core.domain.member.MemberFinder;
import com.ticket.core.enums.EntityStatus;
import com.ticket.core.enums.PerformanceSeatState;
import com.ticket.core.enums.PerformanceState;
import com.ticket.core.support.exception.ErrorType;
import com.ticket.core.support.exception.NotFoundException;
import com.ticket.storage.db.core.PerformanceEntity;
import com.ticket.storage.db.core.PerformanceRepository;
import com.ticket.storage.db.core.PerformanceSeatEntity;
import com.ticket.storage.db.core.PerformanceSeatRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

//@Service
public class ReservationServiceV1 implements ReservationService {

    private final MemberFinder memberFinder;
    private final PerformanceRepository performanceRepository;
    private final PerformanceSeatRepository performanceSeatRepository;
    private final ReservationValidator reservationValidator;
    private final ReservationManager reservationManager;

    public ReservationServiceV1(final MemberFinder memberFinder,
                                final PerformanceRepository performanceRepository,
                                final PerformanceSeatRepository performanceSeatRepository,
                                final ReservationValidator reservationValidator,
                                final ReservationManager reservationManager
) {
        this.memberFinder = memberFinder;
        this.performanceRepository = performanceRepository;
        this.performanceSeatRepository = performanceSeatRepository;
        this.reservationValidator = reservationValidator;
        this.reservationManager = reservationManager;
    }

    @Transactional
    public void addReservation(final NewReservation newReservation) {
        final Member foundMember = memberFinder.find(newReservation.getMemberId());
        final PerformanceEntity foundPerformance = findOpenPerformance(newReservation.getPerformanceId());
        final List<PerformanceSeatEntity> foundPerformanceSeats = findAvailablePerformanceSeats(
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

    private PerformanceEntity findOpenPerformance(final Long performanceId) {
        return performanceRepository.findByIdAndStateAndStatus(
                        performanceId,
                        PerformanceState.OPEN,
                        EntityStatus.ACTIVE
                )
                .orElseThrow(() -> new NotFoundException(ErrorType.NOT_FOUND_DATA));
    }

    private List<PerformanceSeatEntity> findAvailablePerformanceSeats(final List<Long> seatIds, final Long performanceId) {
        return performanceSeatRepository.findByPerformanceIdAndSeatIdInAndState(
                performanceId,
                seatIds,
                PerformanceSeatState.AVAILABLE
        );
    }

}
