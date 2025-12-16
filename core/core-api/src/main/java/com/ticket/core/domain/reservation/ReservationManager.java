package com.ticket.core.domain.reservation;

import com.ticket.core.domain.member.Member;
import com.ticket.storage.db.core.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReservationManager {

    private final ReservationRepository reservationRepository;
    private final ReservationDetailRepository reservationDetailRepository;

    public ReservationManager(final ReservationRepository reservationRepository, final ReservationDetailRepository reservationDetailRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationDetailRepository = reservationDetailRepository;
    }

    public void add(final Member member, final PerformanceEntity performance, final List<PerformanceSeatEntity> performanceSeats) {
        performanceSeats.forEach(PerformanceSeatEntity::reserve);

        final ReservationEntity savedReservation = saveReservation(member, performance);
        saveReservationDetails(performanceSeats, savedReservation.getId());
    }

    private ReservationEntity saveReservation(final Member member, final PerformanceEntity performance) {
        return reservationRepository.save(new ReservationEntity(
                member.getId(),
                performance.getId()
        ));
    }

    private void saveReservationDetails(final List<PerformanceSeatEntity> foundPerformanceSeats, final Long reservationId) {
        reservationDetailRepository.saveAll(
                foundPerformanceSeats.stream()
                        .map(p -> new ReservationDetailEntity(reservationId, p.getId()))
                        .toList()
        );
    }
}
