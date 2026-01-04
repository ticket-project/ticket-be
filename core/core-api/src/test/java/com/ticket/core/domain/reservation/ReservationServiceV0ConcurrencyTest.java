package com.ticket.core.domain.reservation;

import com.ticket.core.domain.performanceseat.PerformanceSeat;
import com.ticket.core.support.ConcurrencyTestBase;
import com.ticket.core.support.ConcurrentTestUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
class ReservationServiceV0ConcurrencyTest extends ConcurrencyTestBase {

    @Autowired
    @Qualifier("reservationServiceV0")
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
    void 재고가_1개일때_여러_요청이_동시에_들어오면_db_update_잠금으로_인해_예매가_오버셀되지_않는다() throws InterruptedException {
        // given
        final List<Long> seatIds = savedPerformanceSeats.stream()
                .map(PerformanceSeat::getSeatId)
                .toList();
        // when & then
        ConcurrentTestUtil.execute(100, idx -> reservationService.addReservation(new NewReservation(
                savedMembers.get(idx).getId(),
                savedPerformance.getId(),
                seatIds
        )));
        final long reservationCount = reservationRepository.count();
        assertThat(reservationCount).isEqualTo(1);
    }
}
