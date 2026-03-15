package com.ticket.core.domain.hold.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
class HoldSnapshotTest {

    @Test
    void 홀드스냅샷은_생성값을_그대로_보관한다() {
        //given
        //when
        LocalDateTime expiresAt = LocalDateTime.of(2026, 3, 15, 12, 30);
        HoldSnapshot holdSnapshot = new HoldSnapshot("hold-key", 1L, 10L, List.of(100L, 101L), expiresAt);

        //then
        assertThat(holdSnapshot.holdKey()).isEqualTo("hold-key");
        assertThat(holdSnapshot.memberId()).isEqualTo(1L);
        assertThat(holdSnapshot.performanceId()).isEqualTo(10L);
        assertThat(holdSnapshot.seatIds()).containsExactly(100L, 101L);
        assertThat(holdSnapshot.expiresAt()).isEqualTo(expiresAt);
    }
}

