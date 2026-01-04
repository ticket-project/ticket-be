package com.ticket.core.domain.hold;

import com.ticket.core.domain.performanceseat.PerformanceSeat;
import com.ticket.core.enums.PerformanceSeatState;
import com.ticket.core.support.ConcurrencyTestBase;
import com.ticket.core.support.ConcurrentTestUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
class HoldServiceV1ConcurrencyTest extends ConcurrencyTestBase {

    @Autowired
    @Qualifier("holdServiceV1")
    private HoldService holdService;

    @Test
    void 동시에_같은_좌석_선점_시도시_분산락에_의해_하나만_성공한다() throws InterruptedException {
        // given & when & then
        ConcurrentTestUtil.execute(100, idx -> holdService.hold(new NewSeatHold(
                savedMembers.get(idx).getId(),
                savedPerformance.getId(),
                savedPerformanceSeats.stream().map(PerformanceSeat::getSeatId).toList())));
    }

    @Test
    void 동시_요청_상황에서_한_요청이_선점_성공_후_좌석_상태가_HELD로_변경된다() throws InterruptedException {
        // given & when
        ConcurrentTestUtil.execute(100, idx -> holdService.hold(new NewSeatHold(
                savedMembers.get(idx).getId(),
                savedPerformance.getId(),
                savedPerformanceSeats.stream().map(PerformanceSeat::getSeatId).toList())));

        // then
        List<PerformanceSeat> seats = performanceSeatRepository.findAllByPerformanceIdAndSeatIdIn(
                savedPerformance.getId(), savedPerformanceSeats.stream().map(PerformanceSeat::getSeatId).toList());

        PerformanceSeat seat = seats.getFirst();
        assertThat(seat.getState()).isEqualTo(PerformanceSeatState.HELD);
        assertThat(seat.getHoldToken()).isNotNull();
        assertThat(seat.getHoldByMemberId()).isNotNull();
        assertThat(seat.getHoldExpireAt()).isAfter(LocalDateTime.now());
    }
}