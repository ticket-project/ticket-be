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

    public void add(final Member member, final ReservationKey reservationKey) {
        reservationKey.getPerformanceSeats().forEach(PerformanceSeatEntity::reserve);

        final ReservationEntity savedReservation = saveReservation(member, reservationKey);
        saveReservationDetails(reservationKey.getPerformanceSeats(), savedReservation.getId());
    }

    private ReservationEntity saveReservation(final Member member, final ReservationKey reservationKey) {
        return reservationRepository.save(new ReservationEntity(
                member.getId(),
                reservationKey.getPerformanceId()
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
