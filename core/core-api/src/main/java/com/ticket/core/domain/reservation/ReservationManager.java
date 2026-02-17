package com.ticket.core.domain.reservation;

import com.ticket.core.domain.performanceseat.PerformanceSeat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ReservationManager {

    private final ReservationRepository reservationRepository;
    private final ReservationDetailRepository reservationDetailRepository;

    public void add(final Long memberId, final Long performanceId, final List<PerformanceSeat> performanceSeats) {
        performanceSeats.forEach(PerformanceSeat::reserve);

        final Reservation savedReservation = saveReservation(memberId, performanceId);
        saveReservationDetails(performanceSeats, savedReservation.getId());
    }

    private Reservation saveReservation(final Long memberId, final Long performanceId) {
        return reservationRepository.save(new Reservation(memberId, performanceId));
    }

    private void saveReservationDetails(final List<PerformanceSeat> foundPerformanceSeats, final Long reservationId) {
        reservationDetailRepository.saveAll(
                foundPerformanceSeats.stream()
                        .map(p -> new ReservationDetail(reservationId, p.getId()))
                        .toList()
        );
    }

    public void addWithoutReserve(final Long memberId, final Long performanceId, final List<PerformanceSeat> reservedSeats) {
        final Reservation savedReservation = saveReservation(memberId, performanceId);
        saveReservationDetails(reservedSeats, savedReservation.getId());
    }
}
