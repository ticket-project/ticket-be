package com.ticket.core.domain.performance;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
class PerformanceTest {

    @Test
    void 요청좌석수가_최대선점수보다_크면_초과다() {
        Performance performance = createPerformance(3, LocalDateTime.now().minusMinutes(10), LocalDateTime.now().plusMinutes(10));

        assertThat(performance.isOverCount(4)).isTrue();
    }

    @Test
    void 요청좌석수가_최대선점수와_같으면_초과가_아니다() {
        Performance performance = createPerformance(3, LocalDateTime.now().minusMinutes(10), LocalDateTime.now().plusMinutes(10));

        assertThat(performance.isOverCount(3)).isFalse();
    }

    @Test
    void 오픈시간과_마감시간_사이면_예매가능하다() {
        Performance performance = createPerformance(3, LocalDateTime.now().minusMinutes(10), LocalDateTime.now().plusMinutes(10));

        assertThat(performance.isBookingOpen(LocalDateTime.now())).isTrue();
    }

    @Test
    void 오픈시각과_같으면_예매가능하다() {
        LocalDateTime now = LocalDateTime.now();
        Performance performance = createPerformance(3, now, now.plusMinutes(10));

        assertThat(performance.isBookingOpen(now)).isTrue();
    }

    @Test
    void 마감시각과_같으면_예매가능하다() {
        LocalDateTime now = LocalDateTime.now();
        Performance performance = createPerformance(3, now.minusMinutes(10), now);

        assertThat(performance.isBookingOpen(now)).isTrue();
    }

    @Test
    void 오픈시간이_없으면_예매불가다() {
        Performance performance = createPerformance(3, null, LocalDateTime.now().plusMinutes(10));

        assertThat(performance.isBookingOpen(LocalDateTime.now())).isFalse();
    }

    @Test
    void 오픈전이면_예매불가다() {
        Performance performance = createPerformance(3, LocalDateTime.now().plusMinutes(10), LocalDateTime.now().plusMinutes(20));

        assertThat(performance.isBookingOpen(LocalDateTime.now())).isFalse();
    }

    @Test
    void 마감후면_예매불가다() {
        Performance performance = createPerformance(3, LocalDateTime.now().minusMinutes(20), LocalDateTime.now().minusMinutes(10));

        assertThat(performance.isBookingOpen(LocalDateTime.now())).isFalse();
    }

    private Performance createPerformance(
            final int maxCanHoldCount,
            final LocalDateTime orderOpenTime,
            final LocalDateTime orderCloseTime
    ) {
        return new Performance(
                null,
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(2),
                orderOpenTime,
                orderCloseTime,
                maxCanHoldCount,
                300
        );
    }
}
