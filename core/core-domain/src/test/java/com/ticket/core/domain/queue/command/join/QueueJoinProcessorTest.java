package com.ticket.core.domain.queue.command.join;

import com.ticket.core.domain.queue.model.QueueEntryStatus;
import com.ticket.core.domain.queue.model.QueueLevel;
import com.ticket.core.domain.queue.runtime.QueueTicket;
import com.ticket.core.domain.queue.runtime.QueueTicketStore;
import com.ticket.core.domain.queue.model.QueueEntryId;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("NonAsciiCharacters")
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
    void 비활성화된_큐는_즉시_입장시킨다() {
        final QueuePolicy policy = new QueuePolicy(false, QueueLevel.LEVEL_1, 300, Duration.ofMinutes(10), Duration.ofHours(1));
        final JoinQueueUseCase.Input input = new JoinQueueUseCase.Input(10L, 101L);
        final QueueTicket admitted = createAdmitted("qe-3", "qt-3");
        when(queueTicketStore.countWaiting(10L)).thenReturn(0L);
        when(queueTicketStore.admitNow(10L, 101L, Duration.ofMinutes(10), Duration.ofHours(1), LocalDateTime.of(2026, 3, 15, 19, 0)))
                .thenReturn(admitted);

        final JoinQueueUseCase.Output output = queueJoinProcessor.join(input, policy);

        assertThat(output.status()).isEqualTo(QueueEntryStatus.ADMITTED);
        assertThat(output.queueToken()).isEqualTo("qt-3");
        verify(queueTicketStore).admitNow(10L, 101L, Duration.ofMinutes(10), Duration.ofHours(1), LocalDateTime.of(2026, 3, 15, 19, 0));
    }

    @Test
    void 수용가능하면_즉시_입장시킨다() {
        final QueuePolicy policy = createPolicy(300);
        final JoinQueueUseCase.Input input = new JoinQueueUseCase.Input(10L, 101L);
        final QueueTicket admitted = createAdmitted("qe-1", "qt-1");
        when(queueTicketStore.countActive(10L)).thenReturn(10L);
        when(queueTicketStore.countWaiting(10L)).thenReturn(0L);
        when(queueTicketStore.admitNow(10L, 101L, Duration.ofMinutes(10), Duration.ofHours(1), LocalDateTime.of(2026, 3, 15, 19, 0)))
                .thenReturn(admitted);

        final JoinQueueUseCase.Output output = queueJoinProcessor.join(input, policy);

        assertThat(output.status()).isEqualTo(QueueEntryStatus.ADMITTED);
        assertThat(output.queueEntryId()).isEqualTo("qe-1");
        assertThat(output.position()).isNull();
    }

    @Test
    void 수용불가면_대기열에_추가한다() {
        final QueuePolicy policy = createPolicy(1);
        final JoinQueueUseCase.Input input = new JoinQueueUseCase.Input(10L, 101L);
        final QueueTicket waiting = new QueueTicket(10L, 101L, "qe-2", QueueEntryStatus.WAITING, 2L, null, null);
        when(queueTicketStore.countActive(10L)).thenReturn(1L);
        when(queueTicketStore.countWaiting(10L)).thenReturn(1L);
        when(queueTicketStore.enqueue(10L, 101L, Duration.ofHours(1))).thenReturn(waiting);
        when(queueTicketStore.findWaitingPosition(10L, QueueEntryId.from("qe-2"))).thenReturn(Optional.of(2L));

        final JoinQueueUseCase.Output output = queueJoinProcessor.join(input, policy);

        assertThat(output.status()).isEqualTo(QueueEntryStatus.WAITING);
        assertThat(output.position()).isEqualTo(2L);
        assertThat(output.queueToken()).isNull();
    }

    @Test
    void 대기순번이_없으면_1을_기본값으로_쓴다() {
        final QueuePolicy policy = createPolicy(1);
        final JoinQueueUseCase.Input input = new JoinQueueUseCase.Input(10L, 101L);
        final QueueTicket waiting = new QueueTicket(10L, 101L, "qe-2", QueueEntryStatus.WAITING, 2L, null, null);
        when(queueTicketStore.countActive(10L)).thenReturn(1L);
        when(queueTicketStore.countWaiting(10L)).thenReturn(1L);
        when(queueTicketStore.enqueue(10L, 101L, Duration.ofHours(1))).thenReturn(waiting);
        when(queueTicketStore.findWaitingPosition(10L, QueueEntryId.from("qe-2"))).thenReturn(Optional.empty());

        final JoinQueueUseCase.Output output = queueJoinProcessor.join(input, policy);

        assertThat(output.position()).isEqualTo(1L);
    }

    @Test
    void 기존_대기자가_있으면_새참가자는_대기열로_보낸다() {
        final QueuePolicy policy = createPolicy(300);
        final JoinQueueUseCase.Input input = new JoinQueueUseCase.Input(10L, 101L);
        final QueueTicket waiting = new QueueTicket(10L, 101L, "qe-wait", QueueEntryStatus.WAITING, 5L, null, null);
        when(queueTicketStore.countActive(10L)).thenReturn(10L);
        when(queueTicketStore.countWaiting(10L)).thenReturn(1L);
        when(queueTicketStore.enqueue(10L, 101L, Duration.ofHours(1))).thenReturn(waiting);
        when(queueTicketStore.findWaitingPosition(10L, QueueEntryId.from("qe-wait"))).thenReturn(Optional.of(5L));

        final JoinQueueUseCase.Output output = queueJoinProcessor.join(input, policy);

        assertThat(output.status()).isEqualTo(QueueEntryStatus.WAITING);
        verify(queueTicketStore).enqueue(10L, 101L, Duration.ofHours(1));
    }

    private QueuePolicy createPolicy(final int maxActiveUsers) {
        return new QueuePolicy(true, QueueLevel.LEVEL_1, maxActiveUsers, Duration.ofMinutes(10), Duration.ofHours(1));
    }

    private QueueTicket createAdmitted(final String queueEntryId, final String queueToken) {
        return new QueueTicket(
                10L,
                101L,
                queueEntryId,
                QueueEntryStatus.ADMITTED,
                null,
                queueToken,
                LocalDateTime.of(2026, 3, 15, 20, 10)
        );
    }
}
