package com.ticket.core.domain.performance;

import com.ticket.core.domain.queue.model.QueueLevel;
import com.ticket.core.domain.queue.model.QueueMode;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
class PerformanceTest {

    @Test
    void 요청좌석수가_최대선점수보다_크면_초과다() {
        //given
        //when
        Performance performance = createPerformance(3, LocalDateTime.now().minusMinutes(10), LocalDateTime.now().plusMinutes(10));

        //then
        assertThat(performance.isOverCount(4)).isTrue();
    }

    @Test
    void 요청좌석수가_최대선점수와_같으면_초과가_아니다() {
        //given
        //when
        Performance performance = createPerformance(3, LocalDateTime.now().minusMinutes(10), LocalDateTime.now().plusMinutes(10));

        //then
        assertThat(performance.isOverCount(3)).isFalse();
    }

    @Test
    void 오픈시간과_마감시간_사이면_예매가능하다() {
        //given
        //when
        Performance performance = createPerformance(3, LocalDateTime.now().minusMinutes(10), LocalDateTime.now().plusMinutes(10));

        //then
        assertThat(performance.isBookingOpen(LocalDateTime.now())).isTrue();
    }

    @Test
    void 오픈시각과_같으면_예매가능하다() {
        //given
        //when
        LocalDateTime now = LocalDateTime.now();
        Performance performance = createPerformance(3, now, now.plusMinutes(10));

        //then
        assertThat(performance.isBookingOpen(now)).isTrue();
    }

    @Test
    void 마감시각과_같으면_예매가능하다() {
        //given
        //when
        LocalDateTime now = LocalDateTime.now();
        Performance performance = createPerformance(3, now.minusMinutes(10), now);

        //then
        assertThat(performance.isBookingOpen(now)).isTrue();
    }

    @Test
    void 오픈시간이_없으면_예매불가다() {
        //given
        //when
        Performance performance = createPerformance(3, null, LocalDateTime.now().plusMinutes(10));

        //then
        assertThat(performance.isBookingOpen(LocalDateTime.now())).isFalse();
    }

    @Test
    void 오픈전이면_예매불가다() {
        //given
        //when
        Performance performance = createPerformance(3, LocalDateTime.now().plusMinutes(10), LocalDateTime.now().plusMinutes(20));

        //then
        assertThat(performance.isBookingOpen(LocalDateTime.now())).isFalse();
    }

    @Test
    void 마감후면_예매불가다() {
        //given
        //when
        Performance performance = createPerformance(3, LocalDateTime.now().minusMinutes(20), LocalDateTime.now().minusMinutes(10));

        //then
        assertThat(performance.isBookingOpen(LocalDateTime.now())).isFalse();
    }

    @Test
    void updateQueuePolicy는_대기열_정책값을_변경한다() {
        // given
        Performance performance = createPerformance(3, LocalDateTime.now().minusMinutes(10), LocalDateTime.now().plusMinutes(10));
        LocalDateTime preopen = LocalDateTime.of(2026, 3, 15, 19, 50);

        // when
        performance.updateQueuePolicy(
                QueueMode.FORCE_ON,
                QueueLevel.LEVEL_2,
                500,
                900,
                preopen,
                "대기열 운영",
                "초기 정책"
        );

        // then
        assertThat(performance.getQueueMode()).isEqualTo(QueueMode.FORCE_ON);
        assertThat(performance.getQueueLevel()).isEqualTo(QueueLevel.LEVEL_2);
        assertThat(performance.getMaxActiveUsers()).isEqualTo(500);
        assertThat(performance.getEntryTokenTtlSeconds()).isEqualTo(900);
        assertThat(performance.getPreopenQueueStartAt()).isEqualTo(preopen);
        assertThat(performance.getWaitingRoomMessage()).isEqualTo("대기열 운영");
        assertThat(performance.getReason()).isEqualTo("초기 정책");
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

