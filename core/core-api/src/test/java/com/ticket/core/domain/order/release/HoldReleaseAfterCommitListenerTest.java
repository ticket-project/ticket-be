package com.ticket.core.domain.order.release;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NonAsciiCharacters")
class HoldReleaseAfterCommitListenerTest {

    @Mock
    private HoldReleaseOutboxExecutor holdReleaseOutboxExecutor;

    @Test
    void 커밋_후_outbox를_즉시_처리한다() {
        final Clock clock = Clock.fixed(Instant.parse("2026-03-25T03:00:00Z"), ZoneId.of("Asia/Seoul"));
        final HoldReleaseAfterCommitListener listener =
                new HoldReleaseAfterCommitListener(holdReleaseOutboxExecutor, clock);

        listener.handleAfterCommit(new HoldReleaseRequestedEvent(99L));

        verify(holdReleaseOutboxExecutor)
                .process(99L, LocalDateTime.of(2026, 3, 25, 12, 0));
    }
}
