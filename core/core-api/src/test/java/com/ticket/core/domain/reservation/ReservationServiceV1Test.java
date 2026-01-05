package com.ticket.core.domain.reservation;

import com.ticket.core.domain.performanceseat.PerformanceSeat;
import com.ticket.core.enums.PerformanceSeatState;
import com.ticket.core.support.IntegrationBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
class ReservationServiceV1Test extends IntegrationBase {

    private static final Logger log = LoggerFactory.getLogger(ReservationServiceV1Test.class);
    @Autowired
    @Qualifier("reservationServiceV1")
    private ReservationService reservationService;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ReservationDetailRepository reservationDetailRepository;

    @AfterEach
    void tearDown() {
        reservationDetailRepository.deleteAllInBatch();
        reservationRepository.deleteAllInBatch();
    }

    @Test
    void 예매를_성공한다() {
        // given
        Long memberId = savedMembers.getFirst().getId();
        Long performanceId = savedPerformance.getId();
        List<Long> seatIds = savedPerformanceSeats.stream()
                .map(PerformanceSeat::getSeatId)
                .toList();
        final NewReservation newReservation = new NewReservation(
                memberId,
                performanceId,
                seatIds
        );
        // when
        reservationService.addReservation(newReservation);
        // then
        final List<PerformanceSeat> reservedSeats = performanceSeatRepository.findAllByPerformanceIdAndSeatIdIn(performanceId, seatIds);
        assertThat(reservedSeats).allMatch(seat -> seat.getState() == PerformanceSeatState.RESERVED);
        final List<Reservation> reservations = reservationRepository.findAll();
        assertThat(reservations).hasSize(1);
        assertThat(reservations.getFirst().getMemberId()).isEqualTo(savedMembers.getFirst().getId());

        final List<ReservationDetail> details = reservationDetailRepository.findAll();
        assertThat(details).hasSize(3);
    }
}
