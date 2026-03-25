package com.ticket.core.domain.order.release;

import com.ticket.core.domain.hold.support.HoldManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
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

    private static final LocalDateTime FIXED_NOW = LocalDateTime.of(2026, 3, 25, 12, 0);

    @Test
    void hold_release에_성공하면_outbox를_완료처리한다() {
        final HoldReleaseOutbox outbox = HoldReleaseOutbox.create(1L, "hold-key", List.of(10L, 20L), FIXED_NOW);
        when(holdReleaseOutboxRepository.findById(1L)).thenReturn(Optional.of(outbox));

        processor().process(1L, FIXED_NOW);

        verify(holdManager).release(1L, "hold-key", List.of(10L, 20L));
        assertThat(outbox.isCompleted()).isTrue();
        assertThat(outbox.getCompletedAt()).isEqualTo(FIXED_NOW);
        assertThat(outbox.getRetryCount()).isZero();
    }

    @Test
    void hold_release에_실패하면_다음_재시도_시각을_기록한다() {
        final HoldReleaseOutbox outbox = HoldReleaseOutbox.create(1L, "hold-key", List.of(10L, 20L), FIXED_NOW);
        when(holdReleaseOutboxRepository.findById(1L)).thenReturn(Optional.of(outbox));
        doThrow(new RuntimeException("release failed")).when(holdManager).release(1L, "hold-key", List.of(10L, 20L));

        processor().process(1L, FIXED_NOW);

        assertThat(outbox.isCompleted()).isFalse();
        assertThat(outbox.getRetryCount()).isEqualTo(1);
        assertThat(outbox.getNextAttemptAt()).isEqualTo(FIXED_NOW.plusSeconds(30));
        assertThat(outbox.getLastError()).contains("release failed");
    }

    private HoldReleaseOutboxProcessor processor() {
        return new HoldReleaseOutboxProcessor(holdReleaseOutboxRepository, holdManager);
    }
}
