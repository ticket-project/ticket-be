package com.ticket.core.domain.reservation;

import com.ticket.core.domain.member.Member;
import com.ticket.core.domain.member.MemberFinder;
import com.ticket.core.domain.performance.Performance;
import com.ticket.core.domain.performance.PerformanceFinder;
import com.ticket.core.enums.PerformanceSeatStatus;
import com.ticket.storage.db.core.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReservationService {

    private final MemberFinder memberFinder;
    private final PerformanceFinder performanceFinder;
    private final PerformanceSeatRepository performanceSeatRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationDetailRepository reservationDetailRepository;

    public ReservationService(final MemberFinder memberFinder,
                              final PerformanceFinder performanceFinder,
                              final PerformanceSeatRepository performanceSeatRepository,
                              final ReservationRepository reservationRepository,
                              final ReservationDetailRepository reservationDetailRepository
    ) {
        this.memberFinder = memberFinder;
        this.performanceFinder = performanceFinder;
        this.performanceSeatRepository = performanceSeatRepository;
        this.reservationRepository = reservationRepository;
        this.reservationDetailRepository = reservationDetailRepository;
    }

    @Transactional
    public void reserve(final NewReservation newReservation) {
        final Member foundMember = memberFinder.find(newReservation.getMemberId());
        final Performance foundPerformance = performanceFinder.find(newReservation.getPerformanceId());
        final List<PerformanceSeatEntity> performanceSeats = performanceSeatRepository.findByPerformanceIdAndSeatIdInAndStatus(
                newReservation.getPerformanceId(), newReservation.getSeatIds(), PerformanceSeatStatus.AVAILABLE
        );

        performanceSeats.forEach(PerformanceSeatEntity::reserve);

        final ReservationEntity savedReservation = reservationRepository.save(new ReservationEntity(foundMember.getId(), foundPerformance.getId()));
        reservationDetailRepository.saveAll(
                performanceSeats.stream()
                .map(p -> new ReservationDetailEntity(savedReservation.getId(), p.getId()))
                .toList()
        );
    }
}
