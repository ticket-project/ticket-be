package com.ticket.core.domain.hold.model;

import com.ticket.core.enums.HoldReleaseReason;
import com.ticket.core.enums.HoldState;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SuppressWarnings("NonAsciiCharacters")
class HoldHistoryTest {

    @Test
    void 홀드이력을_생성하면_active_상태로_초기화된다() {
        //given
        LocalDateTime expiresAt = LocalDateTime.of(2026, 3, 15, 12, 30);

        //when
        HoldHistory holdHistory = createHoldHistory(expiresAt);

        //then
        assertThat(holdHistory.getHoldKey()).isEqualTo("hold-key");
        assertThat(holdHistory.getMemberId()).isEqualTo(1L);
        assertThat(holdHistory.getPerformanceId()).isEqualTo(10L);
        assertThat(holdHistory.getPerformanceSeatId()).isEqualTo(100L);
        assertThat(holdHistory.getSeatId()).isEqualTo(200L);
        assertThat(holdHistory.getStatus()).isEqualTo(HoldState.ACTIVE);
        assertThat(holdHistory.getExpiresAt()).isEqualTo(expiresAt);
    }

    @Test
    void active_홀드이력은_만료처리할_수_있다() {
        //given
        HoldHistory holdHistory = createHoldHistory(LocalDateTime.of(2026, 3, 15, 12, 30));
        LocalDateTime now = LocalDateTime.of(2026, 3, 15, 12, 0);

        //when
        holdHistory.expire(now, HoldReleaseReason.TTL_EXPIRED);

        //then
        assertThat(holdHistory.getStatus()).isEqualTo(HoldState.EXPIRED);
        assertThat(holdHistory.getReleasedAt()).isEqualTo(now);
        assertThat(holdHistory.getReleaseReason()).isEqualTo(HoldReleaseReason.TTL_EXPIRED);
    }

    @Test
    void active_홀드이력은_취소처리할_수_있다() {
        //given
        HoldHistory holdHistory = createHoldHistory(LocalDateTime.of(2026, 3, 15, 12, 30));
        LocalDateTime now = LocalDateTime.of(2026, 3, 15, 12, 0);

        //when
        holdHistory.cancel(now, HoldReleaseReason.USER_CANCELED);

        //then
        assertThat(holdHistory.getStatus()).isEqualTo(HoldState.CANCELED);
        assertThat(holdHistory.getReleasedAt()).isEqualTo(now);
        assertThat(holdHistory.getReleaseReason()).isEqualTo(HoldReleaseReason.USER_CANCELED);
    }

    @Test
    void active가_아닌_홀드이력은_다시_종료처리할_수_없다() {
        //given
        HoldHistory holdHistory = createHoldHistory(LocalDateTime.of(2026, 3, 15, 12, 30));
        holdHistory.expire(LocalDateTime.of(2026, 3, 15, 12, 0), HoldReleaseReason.TTL_EXPIRED);

        //when
        //then
        assertThatThrownBy(() -> holdHistory.cancel(LocalDateTime.of(2026, 3, 15, 12, 5), HoldReleaseReason.USER_CANCELED))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hold history");
    }

    private HoldHistory createHoldHistory(final LocalDateTime expiresAt) {
        return new HoldHistory("hold-key", 1L, 10L, 100L, 200L, expiresAt);
    }
}

