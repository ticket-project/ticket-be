package com.ticket.core.domain.order.release;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.time.ZoneId;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NonAsciiCharacters")
class HoldReleaseOutboxSchedulerTest {

    @Mock
    private HoldReleaseOutboxRepository holdReleaseOutboxRepository;

    @Mock
    private HoldReleaseOutboxProcessor holdReleaseOutboxProcessor;

    @Test
    void 처리할_outbox가_없으면_종료한다() {
        when(holdReleaseOutboxRepository.findAllByCompletedAtIsNullAndNextAttemptAtLessThanEqual(any(LocalDateTime.class), any()))
                .thenReturn(new SliceImpl<>(List.of()));

        scheduler().processPendingHoldReleases();

        verify(holdReleaseOutboxProcessor, times(0)).process(any());
    }

    @Test
    void 처리할_outbox가_있으면_순서대로_처리한다() {
        final HoldReleaseOutbox first = HoldReleaseOutbox.create(1L, "hold-1", List.of(10L), LocalDateTime.of(2026, 3, 25, 12, 0));
        final HoldReleaseOutbox second = HoldReleaseOutbox.create(1L, "hold-2", List.of(20L), LocalDateTime.of(2026, 3, 25, 12, 0));
        ReflectionTestUtils.setField(first, "id", 1L);
        ReflectionTestUtils.setField(second, "id", 2L);
        final Slice<HoldReleaseOutbox> slice = new SliceImpl<>(List.of(first, second));
        when(holdReleaseOutboxRepository.findAllByCompletedAtIsNullAndNextAttemptAtLessThanEqual(any(LocalDateTime.class), any()))
                .thenReturn(slice);

        scheduler().processPendingHoldReleases();

        verify(holdReleaseOutboxProcessor).process(1L);
        verify(holdReleaseOutboxProcessor).process(2L);
    }

    private HoldReleaseOutboxScheduler scheduler() {
        final Clock clock = Clock.fixed(Instant.parse("2026-03-25T03:00:00Z"), ZoneId.of("Asia/Seoul"));
        return new HoldReleaseOutboxScheduler(holdReleaseOutboxRepository, holdReleaseOutboxProcessor, clock);
    }
}
