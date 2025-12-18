package com.ticket.core.domain.reservation;

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

    public void add(final Long memberId, final Long performanceId, final List<PerformanceSeatEntity> performanceSeats) {
        performanceSeats.forEach(PerformanceSeatEntity::reserve);

        final ReservationEntity savedReservation = saveReservation(memberId, performanceId);
        saveReservationDetails(performanceSeats, savedReservation.getId());
    }

    private ReservationEntity saveReservation(final Long memberId, final Long performanceId) {
        return reservationRepository.save(new ReservationEntity(memberId, performanceId));
    }

    private void saveReservationDetails(final List<PerformanceSeatEntity> foundPerformanceSeats, final Long reservationId) {
        reservationDetailRepository.saveAll(
                foundPerformanceSeats.stream()
                        .map(p -> new ReservationDetailEntity(reservationId, p.getId()))
                        .toList()
        );
    }
}
