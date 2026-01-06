package com.ticket.core.domain.hold;

import com.ticket.core.domain.performanceseat.PerformanceSeat;
import com.ticket.core.enums.PerformanceSeatState;
import com.ticket.core.support.ConcurrentTestUtil;
import com.ticket.core.support.IntegrationBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
class HoldServiceV1ConcurrencyTest extends IntegrationBase {

    @Autowired
    private HoldServiceV1 holdService;

    @Test
    void 동시에_같은_좌석_선점_시도시_분산락에_의해_하나만_성공한다() throws InterruptedException {
        // given
        final List<Long> seatIds = savedPerformanceSeats.stream()
                .map(PerformanceSeat::getSeatId)
                .toList();
        // when
        ConcurrentTestUtil.execute(100, idx -> holdService.hold(new NewSeatHold(
                savedMembers.get(idx).getId(),
                savedPerformance.getId(),
                seatIds
        )));

        List<PerformanceSeat> seats = performanceSeatRepository.findAllByPerformanceIdAndSeatIdIn(
                savedPerformance.getId(), savedPerformanceSeats.stream().map(PerformanceSeat::getSeatId).toList());

        // then
        assertThat(seats).hasSize(savedPerformanceSeats.size());
        assertThat(seats).allSatisfy(seat -> {
            assertThat(seat.getState()).isEqualTo(PerformanceSeatState.HELD);
            assertThat(seat.getHoldByMemberId()).isNotNull();
            assertThat(seat.getHoldExpireAt()).isAfter(LocalDateTime.now());
        });

        // 모든 좌석이 같은 회원에게 선점되었는지 확인
        assertThat(seats.stream().map(PerformanceSeat::getHoldByMemberId).distinct())
                .hasSize(1);
    }
}
