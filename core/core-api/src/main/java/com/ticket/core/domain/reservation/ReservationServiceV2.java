package com.ticket.core.domain.reservation;

import com.ticket.core.domain.member.Member;
import com.ticket.core.domain.member.MemberFinder;
import com.ticket.core.enums.EntityStatus;
import com.ticket.core.enums.PerformanceSeatState;
import com.ticket.core.enums.PerformanceState;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import com.ticket.core.support.exception.NotFoundException;
import com.ticket.storage.db.core.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservationServiceV2 implements ReservationService {

    private final MemberFinder memberFinder;
    private final PerformanceRepository performanceRepository;
    private final PerformanceSeatRepository performanceSeatRepository;
    private final SeatHoldRepository seatHoldRepository;

    public ReservationServiceV2(final MemberFinder memberFinder,
                                final PerformanceRepository performanceRepository,
                                final PerformanceSeatRepository performanceSeatRepository,
                                final SeatHoldRepository seatHoldRepository
) {
        this.memberFinder = memberFinder;
        this.performanceRepository = performanceRepository;
        this.performanceSeatRepository = performanceSeatRepository;
        this.seatHoldRepository = seatHoldRepository;
    }

    //HOLD
    @Transactional
    public void addReservation(final NewReservation newReservation) {
        final Member foundMember = memberFinder.find(newReservation.getMemberId());
        final PerformanceEntity foundPerformance = findOpenPerformance(newReservation.getPerformanceId());
        final List<PerformanceSeatEntity> availablePerformanceSeats = findAvailablePerformanceSeats(newReservation.getSeatIds(), foundPerformance.getId());
        if (availablePerformanceSeats.isEmpty()) {
            throw new CoreException(ErrorType.NOT_FOUND_DATA, "가능한 좌석이 없습니다.");
        }
        availablePerformanceSeats.forEach(PerformanceSeatEntity::reserve);

        final List<SeatHoldEntity> seatHoldEntities = availablePerformanceSeats.stream()
                .map(m -> new SeatHoldEntity(foundMember.getId(), m.getId(), LocalDateTime.now().plusMinutes(5)))
                .toList();
        seatHoldRepository.saveAll(seatHoldEntities);

        //이제 reservation은 결제 이후 저장됨.
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
