//package com.ticket.core.domain.reservation;
//
//import com.ticket.core.domain.performanceseat.PerformanceSeat;
//import com.ticket.core.domain.seat.Seat;
//import com.ticket.core.support.ConcurrentTestUtil;
//import com.ticket.core.support.IntegrationBase;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@SuppressWarnings("NonAsciiCharacters")
//class ReservationServiceV1ConcurrencyTest extends IntegrationBase {
//
//    @Autowired
//    @Qualifier("reservationServiceV1")
//    private ReservationService reservationService;
//    @Autowired
//    private ReservationRepository reservationRepository;
//    @Autowired
//    private ReservationDetailRepository reservationDetailRepository;
//
//    @AfterEach
//    void tearDown() {
//        reservationDetailRepository.deleteAllInBatch();
//        reservationRepository.deleteAllInBatch();
//    }
//
//    @Test
//    void 재고가_1개일때_여러_요청이_동시에_들어오면_비관적_락에_의해_예매가_오버셀되지_않는다() throws InterruptedException {
//        // given
//        final List<Long> seatIds = savedPerformanceSeats.stream()
//                .map(PerformanceSeat::getSeat)
//                .map(Seat::getId)
//                .toList();
//        // given & when & then
//        ConcurrentTestUtil.execute(100, idx -> reservationService.addReservation(new NewReservation(
//                savedMembers.get(idx).getId(),
//                savedPerformance.getId(),
//                seatIds
//        )));
//        final long reservationCount = reservationRepository.count();
//        assertThat(reservationCount).isEqualTo(1);
//    }
//
//}
