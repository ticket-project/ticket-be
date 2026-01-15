//package com.ticket.core.domain.hold;
//
//import com.ticket.core.domain.performanceseat.PerformanceSeat;
//import com.ticket.core.domain.seat.Seat;
//import com.ticket.core.enums.PerformanceSeatState;
//import com.ticket.core.support.ConcurrentTestUtil;
//import com.ticket.core.support.IntegrationBase;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@SuppressWarnings("NonAsciiCharacters")
//class HoldLockServiceConcurrencyTest extends IntegrationBase {
//
//    @Autowired
//    private HoldLockService holdLockService;
//
//    @Test
//    void 재고가_1개일때_여러_요청이_동시에_들어오면_비관적_락에_의해_한_명만_선점한다() throws InterruptedException {
//        // given
//        final List<Long> seatIds = savedPerformanceSeats.stream()
//                .map(PerformanceSeat::getSeat)
//                .map(Seat::getId)
//                .toList();
//        // when & then
//        ConcurrentTestUtil.execute(100, idx -> holdLockService.hold(1L, new NewSeatHold(
//                savedPerformance.getId(),
//                seatIds
//        )));
//    }
//
//    @Test
//    void 동시에_같은_좌석_선점_시도시_분산락에_의해_하나만_성공한다() throws InterruptedException {
//        // given
//        final List<Long> seatIds = savedPerformanceSeats.stream()
//                .map(PerformanceSeat::getSeat)
//                .map(Seat::getId)
//                .toList();
//        // when
//        ConcurrentTestUtil.execute(100, idx -> holdLockService.hold(1L, new NewSeatHold(
//                savedPerformance.getId(),
//                seatIds
//        )));
//
//        List<PerformanceSeat> seats = performanceSeatRepository.findAllByPerformanceIdAndSeatIdIn(
//                savedPerformance.getId(), savedPerformanceSeats.stream().map(PerformanceSeat::getSeat).map(Seat::getId).toList());
//
//        // then
//        assertThat(seats).hasSize(savedPerformanceSeats.size());
//        assertThat(seats).allSatisfy(seat -> {
//            assertThat(seat.getState()).isEqualTo(PerformanceSeatState.HELD);
//        });
//
//    }
//
//}
