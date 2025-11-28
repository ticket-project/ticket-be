package com.ticket.reservation;

import com.ticket.core.domain.performance.Performance;
import com.ticket.core.domain.performanceseat.PerformanceSeat;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static com.ticket.util.TestCommonUtils.*;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SuppressWarnings("NonAsciiCharacters")
public class ReservationTest {

    @Test
    void 재고가_있고_공연_회차_시작_전이면_예매를_성공한다() {
        //given
        Long memberId = 1L;
        int seatCount = 4;
        final Performance performance = new Performance(performanceStartTime(), performanceEndTime(), reserveOpenTimeCurrent(), reserveCloseTimeCurrent());
        final PerformanceSeat performanceSeat = new PerformanceSeat(performance, seatCount);
        //when
        final LocalDateTime currTime = currentTime();
        final boolean isReserved = performanceSeat.reserve(memberId, currTime);
        //then
        assertThat(isReserved).isTrue();
    }

    @Test
    void 재고가_없다면_예매를_실패한다() {
        //given
        Long memberId = 1L;
        int seatCount = 0;
        final Performance performance = new Performance(currentTime().minusHours(10), currentTime().minusHours(5), reserveOpenTimeFuture(), reserveCloseTimeFuture());
        final PerformanceSeat performanceSeat = new PerformanceSeat(performance, seatCount);

        final LocalDateTime currTime = currentTime();
        //when
        boolean isReserved = performanceSeat.reserve(memberId, currTime);
        //then
        assertThat(isReserved).isFalse();
    }

    @Test
    void 종료된_회차를_예매하려고_하면_실패한다() {
        //given
        Long memberId = 1L;
        int seatCount = 0;
        final Performance performance = new Performance(currentTime().minusHours(10), currentTime().minusHours(5), reserveOpenTimeFuture(), reserveCloseTimeFuture());
        final PerformanceSeat performanceSeat = new PerformanceSeat(performance, seatCount);

        final LocalDateTime currTime = currentTime();
        //when
        boolean isReserved = performanceSeat.reserve(memberId, currTime);
        //then
        assertThat(isReserved).isFalse();
    }
}
