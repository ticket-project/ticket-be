package com.ticket.core.domain.hold.support;

import com.ticket.core.domain.hold.application.HoldKeyGenerator;
import com.ticket.core.domain.hold.model.HoldSnapshot;
import com.ticket.core.domain.hold.store.HoldStore;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("NonAsciiCharacters")
@ExtendWith(MockitoExtension.class)
class HoldManagerTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-03-15T10:00:00Z"), ZoneId.of("Asia/Seoul"));

    @Mock
    private HoldStore holdStore;

    @Mock
    private HoldKeyGenerator holdKeyGenerator;

    private HoldManager holdManager;

    @BeforeEach
    void setUp() {
        this.holdManager = new HoldManager(holdStore, holdKeyGenerator, FIXED_CLOCK);
    }

    @Test
    void 이미_hold된_좌석이_있으면_SEAT_ALREADY_HOLD_예외를_던진다() {
        //given
        when(holdKeyGenerator.generate()).thenReturn("hold-key");
        when(holdStore.isHeld(1L, 10L)).thenReturn(true);

        //when
        //then
        assertThatThrownBy(() -> holdManager.createHold(1L, 1L, List.of(10L), Duration.ofMinutes(5)))
                .isInstanceOf(CoreException.class)
                .satisfies(thrown -> assertThat(((CoreException) thrown).getErrorType()).isEqualTo(ErrorType.SEAT_ALREADY_HOLD));
    }

    @Test
    void hold를_생성하면_좌석키와_메타키를_저장하고_snapshot을_반환한다() {
        //given
        Duration ttl = Duration.ofMinutes(5);

        when(holdKeyGenerator.generate()).thenReturn("hold-key");

        //when
        HoldSnapshot snapshot = holdManager.createHold(7L, 1L, List.of(10L, 20L), ttl);

        //then
        assertThat(snapshot.holdKey()).isEqualTo("hold-key");
        assertThat(snapshot.memberId()).isEqualTo(7L);
        assertThat(snapshot.performanceId()).isEqualTo(1L);
        assertThat(snapshot.seatIds()).containsExactly(10L, 20L);
        assertThat(snapshot.expiresAt()).isEqualTo(LocalDateTime.of(2026, 3, 15, 19, 5));
        verify(holdStore).save(snapshot, ttl);
    }

    @Test
    void release는_중복좌석을_정렬해_전달한다() {
        //given
        //when
        holdManager.release(1L, "hold-key", List.of(20L, 10L, 10L));

        //then
        verify(holdStore).release(1L, "hold-key", List.of(10L, 20L));
    }

    @Test
    void 현재_hold중인_좌석아이디들을_조회한다() {
        //given
        when(holdStore.getHoldingSeatIds(1L)).thenReturn(java.util.Set.of(10L, 30L));

        //when
        Set<Long> result = holdManager.getHoldingSeatIds(1L);

        //then
        assertThat(result).containsExactlyInAnyOrder(10L, 30L);
    }

    @Test
    void isHeld는_bucket값_존재여부를_반환한다() {
        //given
        when(holdStore.isHeld(1L, 10L)).thenReturn(true);

        //when
        boolean result = holdManager.isHeld(1L, 10L);

        //then
        assertThat(result).isTrue();
    }
}

