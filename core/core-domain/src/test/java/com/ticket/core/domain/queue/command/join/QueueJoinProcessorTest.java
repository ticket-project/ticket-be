package com.ticket.core.domain.queue.command.join;

import com.ticket.core.domain.queue.model.QueueEntryStatus;
import com.ticket.core.domain.queue.model.QueueLevel;
import com.ticket.core.domain.queue.runtime.QueueJoinResult;
import com.ticket.core.domain.queue.runtime.QueueTicketStore;
import com.ticket.core.domain.queue.support.QueuePolicy;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QueueJoinProcessorTest {

    @Mock
    private QueueTicketStore queueTicketStore;

    private QueueJoinProcessor queueJoinProcessor;
    private final Clock fixedClock = Clock.fixed(Instant.parse("2026-03-15T10:00:00Z"), ZoneId.of("Asia/Seoul"));

    @BeforeEach
    void setUp() {
        queueJoinProcessor = new QueueJoinProcessor(queueTicketStore, fixedClock);
    }

    @Test
    void join_uses_atomic_enter_operation() {
        final QueuePolicy policy = createPolicy(300);
        final JoinQueueUseCase.Input input = new JoinQueueUseCase.Input(10L, 101L);
        final QueueJoinResult result = new QueueJoinResult(
                QueueEntryStatus.WAITING,
                "qe-atomic",
                4L,
                null,
                null
        );
        when(queueTicketStore.enter(10L, 101L, policy, LocalDateTime.of(2026, 3, 15, 19, 0)))
                .thenReturn(result);

        final JoinQueueUseCase.Output output = queueJoinProcessor.join(input, policy);

        assertThat(output.status()).isEqualTo(QueueEntryStatus.WAITING);
        assertThat(output.queueEntryId()).isEqualTo("qe-atomic");
        assertThat(output.position()).isEqualTo(4L);
        assertThat(output.queueToken()).isNull();
        verify(queueTicketStore).enter(10L, 101L, policy, LocalDateTime.of(2026, 3, 15, 19, 0));
    }

    @Test
    void join_returns_admitted_token_from_atomic_enter_result() {
        final QueuePolicy policy = createPolicy(300);
        final JoinQueueUseCase.Input input = new JoinQueueUseCase.Input(10L, 101L);
        final LocalDateTime expiresAt = LocalDateTime.of(2026, 3, 15, 19, 10);
        final QueueJoinResult result = new QueueJoinResult(
                QueueEntryStatus.ADMITTED,
                "qe-admitted",
                null,
                "qt-admitted",
                expiresAt
        );
        when(queueTicketStore.enter(10L, 101L, policy, LocalDateTime.of(2026, 3, 15, 19, 0)))
                .thenReturn(result);

        final JoinQueueUseCase.Output output = queueJoinProcessor.join(input, policy);

        assertThat(output.status()).isEqualTo(QueueEntryStatus.ADMITTED);
        assertThat(output.queueEntryId()).isEqualTo("qe-admitted");
        assertThat(output.position()).isNull();
        assertThat(output.queueToken()).isEqualTo("qt-admitted");
        assertThat(output.expiresAt()).isEqualTo(expiresAt);
    }

    private QueuePolicy createPolicy(final int maxActiveUsers) {
        return new QueuePolicy(true, QueueLevel.LEVEL_1, maxActiveUsers, Duration.ofMinutes(10), Duration.ofHours(1));
    }
}
