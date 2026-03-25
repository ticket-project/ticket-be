package com.ticket.core.domain.queue.usecase;

import com.ticket.core.domain.queue.command.QueueAdmissionAdvancer;
import com.ticket.core.domain.queue.model.QueueEntryStatus;
import com.ticket.core.domain.queue.model.QueueLevel;
import com.ticket.core.domain.queue.runtime.QueueTicket;
import com.ticket.core.domain.queue.runtime.QueueTicketStore;
import com.ticket.core.domain.queue.support.QueuePolicy;
import com.ticket.core.domain.queue.support.QueuePolicyResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("NonAsciiCharacters")
@ExtendWith(MockitoExtension.class)
class JoinQueueUseCaseTest {

    @Mock
    private QueuePolicyResolver queuePolicyResolver;

    @Mock
    private QueueTicketStore queueTicketStore;

    @Mock
    private QueueAdmissionAdvancer queueAdmissionAdvancer;

    @InjectMocks
    private JoinQueueUseCase joinQueueUseCase;

    @Test
    void disabled_queue_admits_immediately() {
        QueuePolicy policy = new QueuePolicy(false, QueueLevel.LEVEL_1, 300, Duration.ofMinutes(10), Duration.ofHours(1));
        QueueTicket admitted = createAdmitted("qe-3", "qt-3");

        when(queuePolicyResolver.resolve(10L)).thenReturn(policy);
        when(queueTicketStore.countWaiting(10L)).thenReturn(0L);
        when(queueTicketStore.admitNow(10L, 101L, Duration.ofMinutes(10), Duration.ofHours(1))).thenReturn(admitted);

        JoinQueueUseCase.Output output = joinQueueUseCase.execute(new JoinQueueUseCase.Input(10L, 101L));

        assertThat(output.status()).isEqualTo(QueueEntryStatus.ADMITTED);
        assertThat(output.queueToken()).isEqualTo("qt-3");
        verify(queueTicketStore).admitNow(10L, 101L, Duration.ofMinutes(10), Duration.ofHours(1));
    }

    @Test
    void available_capacity_admits_immediately() {
        QueuePolicy policy = createPolicy(300);
        QueueTicket admitted = createAdmitted("qe-1", "qt-1");

        when(queuePolicyResolver.resolve(10L)).thenReturn(policy);
        when(queueTicketStore.countActive(10L)).thenReturn(10L);
        when(queueTicketStore.countWaiting(10L)).thenReturn(0L);
        when(queueTicketStore.admitNow(10L, 101L, Duration.ofMinutes(10), Duration.ofHours(1))).thenReturn(admitted);

        JoinQueueUseCase.Output output = joinQueueUseCase.execute(new JoinQueueUseCase.Input(10L, 101L));

        assertThat(output.status()).isEqualTo(QueueEntryStatus.ADMITTED);
        assertThat(output.queueEntryId()).isEqualTo("qe-1");
        assertThat(output.position()).isNull();
    }

    @Test
    void full_capacity_enqueues_waiting_entry() {
        QueuePolicy policy = createPolicy(1);
        QueueTicket waiting = new QueueTicket(10L, 101L, "qe-2", QueueEntryStatus.WAITING, 2L, null, null);

        when(queuePolicyResolver.resolve(10L)).thenReturn(policy);
        when(queueTicketStore.countActive(10L)).thenReturn(1L);
        when(queueTicketStore.countWaiting(10L)).thenReturn(1L);
        when(queueTicketStore.enqueue(10L, 101L, Duration.ofHours(1))).thenReturn(waiting);
        when(queueTicketStore.findWaitingPosition(10L, QueueEntryId.from("qe-2"))).thenReturn(Optional.of(2L));

        JoinQueueUseCase.Output output = joinQueueUseCase.execute(new JoinQueueUseCase.Input(10L, 101L));

        assertThat(output.status()).isEqualTo(QueueEntryStatus.WAITING);
        assertThat(output.position()).isEqualTo(2L);
        assertThat(output.queueToken()).isNull();
    }

    @Test
    void missing_waiting_position_defaults_to_one() {
        QueuePolicy policy = createPolicy(1);
        QueueTicket waiting = new QueueTicket(10L, 101L, "qe-2", QueueEntryStatus.WAITING, 2L, null, null);

        when(queuePolicyResolver.resolve(10L)).thenReturn(policy);
        when(queueTicketStore.countActive(10L)).thenReturn(1L);
        when(queueTicketStore.countWaiting(10L)).thenReturn(1L);
        when(queueTicketStore.enqueue(10L, 101L, Duration.ofHours(1))).thenReturn(waiting);
        when(queueTicketStore.findWaitingPosition(10L, QueueEntryId.from("qe-2"))).thenReturn(Optional.empty());

        JoinQueueUseCase.Output output = joinQueueUseCase.execute(new JoinQueueUseCase.Input(10L, 101L));

        assertThat(output.position()).isEqualTo(1L);
    }

    @Test
    void rejoin_waiting_member_clears_old_waiting_entry() {
        QueuePolicy policy = createPolicy(1);
        QueueTicket previous = new QueueTicket(10L, 101L, "qe-old", QueueEntryStatus.WAITING, 1L, null, null);
        QueueTicket waiting = new QueueTicket(10L, 101L, "qe-new", QueueEntryStatus.WAITING, 2L, null, null);

        when(queuePolicyResolver.resolve(10L)).thenReturn(policy);
        when(queueTicketStore.findMemberEntryId(10L, 101L)).thenReturn(Optional.of("qe-old"));
        when(queueTicketStore.findEntry(QueueEntryId.from("qe-old"))).thenReturn(Optional.of(previous));
        when(queueTicketStore.countActive(10L)).thenReturn(1L);
        when(queueTicketStore.countWaiting(10L)).thenReturn(1L);
        when(queueTicketStore.enqueue(10L, 101L, Duration.ofHours(1))).thenReturn(waiting);
        when(queueTicketStore.findWaitingPosition(10L, QueueEntryId.from("qe-new"))).thenReturn(Optional.of(2L));

        JoinQueueUseCase.Output output = joinQueueUseCase.execute(new JoinQueueUseCase.Input(10L, 101L));

        verify(queueTicketStore).leaveWaiting(10L, QueueEntryId.from("qe-old"));
        verify(queueAdmissionAdvancer, never()).advance(10L);
        assertThat(output.queueEntryId()).isEqualTo("qe-new");
    }

    @Test
    void rejoin_admitted_member_revokes_token_and_requeues() {
        QueuePolicy policy = createPolicy(2);
        QueueTicket previous = new QueueTicket(
                10L,
                101L,
                "qe-old",
                QueueEntryStatus.ADMITTED,
                null,
                "qt-old",
                LocalDateTime.of(2026, 3, 15, 20, 0)
        );
        QueueTicket waiting = new QueueTicket(10L, 101L, "qe-new", QueueEntryStatus.WAITING, 3L, null, null);

        when(queuePolicyResolver.resolve(10L)).thenReturn(policy);
        when(queueTicketStore.findMemberEntryId(10L, 101L)).thenReturn(Optional.of("qe-old"));
        when(queueTicketStore.findEntry(QueueEntryId.from("qe-old"))).thenReturn(Optional.of(previous));
        when(queueTicketStore.countActive(10L)).thenReturn(2L);
        when(queueTicketStore.countWaiting(10L)).thenReturn(1L);
        when(queueTicketStore.enqueue(10L, 101L, Duration.ofHours(1))).thenReturn(waiting);
        when(queueTicketStore.findWaitingPosition(10L, QueueEntryId.from("qe-new"))).thenReturn(Optional.of(4L));

        JoinQueueUseCase.Output output = joinQueueUseCase.execute(new JoinQueueUseCase.Input(10L, 101L));

        verify(queueTicketStore).leaveAdmitted(10L, QueueEntryId.from("qe-old"), "qt-old");
        verify(queueAdmissionAdvancer).advance(10L);
        assertThat(output.status()).isEqualTo(QueueEntryStatus.WAITING);
        assertThat(output.queueEntryId()).isEqualTo("qe-new");
    }

    @Test
    void missing_previous_entry_clears_member_mapping() {
        QueuePolicy policy = createPolicy(1);
        QueueTicket waiting = new QueueTicket(10L, 101L, "qe-new", QueueEntryStatus.WAITING, 1L, null, null);

        when(queuePolicyResolver.resolve(10L)).thenReturn(policy);
        when(queueTicketStore.findMemberEntryId(10L, 101L)).thenReturn(Optional.of("missing"));
        when(queueTicketStore.findEntry(QueueEntryId.from("missing"))).thenReturn(Optional.empty());
        when(queueTicketStore.countActive(10L)).thenReturn(1L);
        when(queueTicketStore.countWaiting(10L)).thenReturn(0L);
        when(queueTicketStore.enqueue(10L, 101L, Duration.ofHours(1))).thenReturn(waiting);
        when(queueTicketStore.findWaitingPosition(10L, QueueEntryId.from("qe-new"))).thenReturn(Optional.of(1L));

        joinQueueUseCase.execute(new JoinQueueUseCase.Input(10L, 101L));

        verify(queueTicketStore).clearMemberEntry(10L, 101L);
    }

    @Test
    void stale_previous_entry_clears_member_mapping() {
        QueuePolicy policy = createPolicy(1);
        QueueTicket previous = new QueueTicket(10L, 101L, "qe-old", QueueEntryStatus.LEFT, 1L, null, null);
        QueueTicket waiting = new QueueTicket(10L, 101L, "qe-new", QueueEntryStatus.WAITING, 1L, null, null);

        when(queuePolicyResolver.resolve(10L)).thenReturn(policy);
        when(queueTicketStore.findMemberEntryId(10L, 101L)).thenReturn(Optional.of("qe-old"));
        when(queueTicketStore.findEntry(QueueEntryId.from("qe-old"))).thenReturn(Optional.of(previous));
        when(queueTicketStore.countActive(10L)).thenReturn(1L);
        when(queueTicketStore.countWaiting(10L)).thenReturn(0L);
        when(queueTicketStore.enqueue(10L, 101L, Duration.ofHours(1))).thenReturn(waiting);
        when(queueTicketStore.findWaitingPosition(10L, QueueEntryId.from("qe-new"))).thenReturn(Optional.of(1L));

        joinQueueUseCase.execute(new JoinQueueUseCase.Input(10L, 101L));

        verify(queueTicketStore).clearMemberEntry(10L, 101L);
        verify(queueAdmissionAdvancer, never()).advance(10L);
    }

    @Test
    void existing_waiters_force_new_entry_into_queue() {
        QueuePolicy policy = createPolicy(300);
        QueueTicket waiting = new QueueTicket(10L, 101L, "qe-wait", QueueEntryStatus.WAITING, 5L, null, null);

        when(queuePolicyResolver.resolve(10L)).thenReturn(policy);
        when(queueTicketStore.countActive(10L)).thenReturn(10L);
        when(queueTicketStore.countWaiting(10L)).thenReturn(1L);
        when(queueTicketStore.enqueue(10L, 101L, Duration.ofHours(1))).thenReturn(waiting);
        when(queueTicketStore.findWaitingPosition(10L, QueueEntryId.from("qe-wait"))).thenReturn(Optional.of(5L));

        JoinQueueUseCase.Output output = joinQueueUseCase.execute(new JoinQueueUseCase.Input(10L, 101L));

        assertThat(output.status()).isEqualTo(QueueEntryStatus.WAITING);
        verify(queueTicketStore).enqueue(10L, 101L, Duration.ofHours(1));
    }

    private QueuePolicy createPolicy(final int maxActiveUsers) {
        return new QueuePolicy(
                true,
                QueueLevel.LEVEL_1,
                maxActiveUsers,
                Duration.ofMinutes(10),
                Duration.ofHours(1)
        );
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
