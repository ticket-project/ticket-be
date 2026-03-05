package com.ticket.core.domain.reservation;

import com.ticket.core.domain.performanceseat.PerformanceSeat;
import com.ticket.core.domain.performanceseat.SeatEventPublisher;
import com.ticket.core.domain.performanceseat.SeatStatusMessage;
import com.ticket.core.domain.performanceseat.SeatStatusMessage.SeatAction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ReservationManager {

    private final ReservationRepository reservationRepository;
    private final ReservationDetailRepository reservationDetailRepository;
    private final SeatEventPublisher seatEventPublisher;

    public void add(final Long memberId, final Long performanceId, final List<PerformanceSeat> performanceSeats) {
        performanceSeats.forEach(PerformanceSeat::reserve);

        final Reservation savedReservation = saveReservation(memberId, performanceId);
        saveReservationDetails(performanceSeats, savedReservation.getId());

        // 예약 성공 이벤트 발행
        performanceSeats.forEach(ps ->
                seatEventPublisher.publish(SeatStatusMessage.of(performanceId, ps.getSeat().getId(), SeatAction.RESERVED))
        );
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
