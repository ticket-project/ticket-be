package com.ticket.core.domain.order.release;

import com.ticket.core.domain.hold.support.HoldManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NonAsciiCharacters")
class HoldReleaseOutboxProcessorTest {

    @Mock
    private HoldReleaseOutboxRepository holdReleaseOutboxRepository;

    @Mock
    private HoldManager holdManager;

    private final Clock clock = Clock.fixed(Instant.parse("2026-03-25T03:00:00Z"), ZoneId.of("Asia/Seoul"));

    @Test
    void hold_release에_성공하면_outbox를_완료처리한다() {
        final HoldReleaseOutbox outbox = HoldReleaseOutbox.create(1L, "hold-key", List.of(10L, 20L), now());
        when(holdReleaseOutboxRepository.findById(1L)).thenReturn(Optional.of(outbox));

        processor().process(1L);

        verify(holdManager).release(1L, "hold-key", List.of(10L, 20L));
        assertThat(outbox.isCompleted()).isTrue();
        assertThat(outbox.getCompletedAt()).isEqualTo(now());
        assertThat(outbox.getRetryCount()).isZero();
    }

    @Test
    void hold_release에_실패하면_다음_재시도_시각을_기록한다() {
        final HoldReleaseOutbox outbox = HoldReleaseOutbox.create(1L, "hold-key", List.of(10L, 20L), now());
        when(holdReleaseOutboxRepository.findById(1L)).thenReturn(Optional.of(outbox));
        doThrow(new RuntimeException("release failed")).when(holdManager).release(1L, "hold-key", List.of(10L, 20L));

        processor().process(1L);

        assertThat(outbox.isCompleted()).isFalse();
        assertThat(outbox.getRetryCount()).isEqualTo(1);
        assertThat(outbox.getNextAttemptAt()).isEqualTo(now().plusSeconds(30));
        assertThat(outbox.getLastError()).contains("release failed");
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }

    private HoldReleaseOutboxProcessor processor() {
        return new HoldReleaseOutboxProcessor(holdReleaseOutboxRepository, holdManager, clock);
    }
}
