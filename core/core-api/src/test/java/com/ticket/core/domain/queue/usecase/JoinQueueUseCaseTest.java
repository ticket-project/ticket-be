package com.ticket.core.domain.queue.usecase;

import com.ticket.core.domain.queue.command.QueueAdmissionProcessor;
import com.ticket.core.domain.queue.model.QueueEntryStatus;
import com.ticket.core.domain.queue.model.QueueLevel;
import com.ticket.core.domain.queue.runtime.QueueTicket;
import com.ticket.core.domain.queue.runtime.QueueTicketStore;
import com.ticket.core.domain.queue.support.QueuePolicyResolver;
import com.ticket.core.domain.queue.support.ResolvedQueuePolicy;
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
    private QueueAdmissionProcessor queueAdmissionProcessor;

    @InjectMocks
    private JoinQueueUseCase joinQueueUseCase;

    @Test
    void 대기열이_비활성화된_공연은_즉시_입장한다() {
        ResolvedQueuePolicy policy = new ResolvedQueuePolicy(
                false,
                QueueLevel.LEVEL_1,
                300,
                Duration.ofMinutes(10),
                Duration.ofHours(1)
        );
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
    void active가_최대_인원보다_적으면_즉시_입장한다() {
        ResolvedQueuePolicy policy = createPolicy(300);
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
    void 최대_인원을_초과하면_대기열에_등록한다() {
        ResolvedQueuePolicy policy = createPolicy(1);
        QueueTicket waiting = new QueueTicket(10L, 101L, "qe-2", QueueEntryStatus.WAITING, 2L, null, null);

        when(queuePolicyResolver.resolve(10L)).thenReturn(policy);
        when(queueTicketStore.countActive(10L)).thenReturn(1L);
        when(queueTicketStore.countWaiting(10L)).thenReturn(1L);
        when(queueTicketStore.enqueue(10L, 101L, Duration.ofHours(1))).thenReturn(waiting);
        when(queueTicketStore.findWaitingPosition(10L, "qe-2")).thenReturn(Optional.of(2L));

        JoinQueueUseCase.Output output = joinQueueUseCase.execute(new JoinQueueUseCase.Input(10L, 101L));

        assertThat(output.status()).isEqualTo(QueueEntryStatus.WAITING);
        assertThat(output.position()).isEqualTo(2L);
        assertThat(output.queueToken()).isNull();
    }

    @Test
    void 대기순번을_찾지_못하면_1번으로_처리한다() {
        ResolvedQueuePolicy policy = createPolicy(1);
        QueueTicket waiting = new QueueTicket(10L, 101L, "qe-2", QueueEntryStatus.WAITING, 2L, null, null);

        when(queuePolicyResolver.resolve(10L)).thenReturn(policy);
        when(queueTicketStore.countActive(10L)).thenReturn(1L);
        when(queueTicketStore.countWaiting(10L)).thenReturn(1L);
        when(queueTicketStore.enqueue(10L, 101L, Duration.ofHours(1))).thenReturn(waiting);
        when(queueTicketStore.findWaitingPosition(10L, "qe-2")).thenReturn(Optional.empty());

        JoinQueueUseCase.Output output = joinQueueUseCase.execute(new JoinQueueUseCase.Input(10L, 101L));

        assertThat(output.position()).isEqualTo(1L);
    }

    @Test
    void 같은_회원이_재진입하면_기존_대기_엔트리를_정리하고_다시_등록한다() {
        ResolvedQueuePolicy policy = createPolicy(1);
        QueueTicket previous = new QueueTicket(10L, 101L, "qe-old", QueueEntryStatus.WAITING, 1L, null, null);
        QueueTicket waiting = new QueueTicket(10L, 101L, "qe-new", QueueEntryStatus.WAITING, 2L, null, null);

        when(queuePolicyResolver.resolve(10L)).thenReturn(policy);
        when(queueTicketStore.findMemberEntryId(10L, 101L)).thenReturn(Optional.of("qe-old"));
        when(queueTicketStore.findEntry("qe-old")).thenReturn(Optional.of(previous));
        when(queueTicketStore.countActive(10L)).thenReturn(1L);
        when(queueTicketStore.countWaiting(10L)).thenReturn(1L);
        when(queueTicketStore.enqueue(10L, 101L, Duration.ofHours(1))).thenReturn(waiting);
        when(queueTicketStore.findWaitingPosition(10L, "qe-new")).thenReturn(Optional.of(2L));

        JoinQueueUseCase.Output output = joinQueueUseCase.execute(new JoinQueueUseCase.Input(10L, 101L));

        verify(queueTicketStore).leaveWaiting(10L, "qe-old");
        verify(queueAdmissionProcessor, never()).advance(10L);
        assertThat(output.queueEntryId()).isEqualTo("qe-new");
    }

    @Test
    void 같은_회원이_입장_상태에서_재진입하면_기존_토큰을_회수하고_대기열에_다시_넣는다() {
        ResolvedQueuePolicy policy = createPolicy(2);
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
        when(queueTicketStore.findEntry("qe-old")).thenReturn(Optional.of(previous));
        when(queueTicketStore.countActive(10L)).thenReturn(2L);
        when(queueTicketStore.countWaiting(10L)).thenReturn(1L);
        when(queueTicketStore.enqueue(10L, 101L, Duration.ofHours(1))).thenReturn(waiting);
        when(queueTicketStore.findWaitingPosition(10L, "qe-new")).thenReturn(Optional.of(4L));

        JoinQueueUseCase.Output output = joinQueueUseCase.execute(new JoinQueueUseCase.Input(10L, 101L));

        verify(queueTicketStore).leaveAdmitted(10L, "qe-old", "qt-old");
        verify(queueAdmissionProcessor).advance(10L);
        assertThat(output.status()).isEqualTo(QueueEntryStatus.WAITING);
        assertThat(output.queueEntryId()).isEqualTo("qe-new");
    }

    @Test
    void 재진입_시_멤버_매핑만_남아있으면_매핑만_정리한다() {
        ResolvedQueuePolicy policy = createPolicy(1);
        QueueTicket waiting = new QueueTicket(10L, 101L, "qe-new", QueueEntryStatus.WAITING, 1L, null, null);

        when(queuePolicyResolver.resolve(10L)).thenReturn(policy);
        when(queueTicketStore.findMemberEntryId(10L, 101L)).thenReturn(Optional.of("missing"));
        when(queueTicketStore.findEntry("missing")).thenReturn(Optional.empty());
        when(queueTicketStore.countActive(10L)).thenReturn(1L);
        when(queueTicketStore.countWaiting(10L)).thenReturn(0L);
        when(queueTicketStore.enqueue(10L, 101L, Duration.ofHours(1))).thenReturn(waiting);
        when(queueTicketStore.findWaitingPosition(10L, "qe-new")).thenReturn(Optional.of(1L));

        joinQueueUseCase.execute(new JoinQueueUseCase.Input(10L, 101L));

        verify(queueTicketStore).clearMemberEntry(10L, 101L);
    }

    @Test
    void 대기자가_있으면_active_여유가_있어도_신규_진입자는_대기열에_선다() {
        ResolvedQueuePolicy policy = createPolicy(300);
        QueueTicket waiting = new QueueTicket(10L, 101L, "qe-wait", QueueEntryStatus.WAITING, 5L, null, null);

        when(queuePolicyResolver.resolve(10L)).thenReturn(policy);
        when(queueTicketStore.countActive(10L)).thenReturn(10L);
        when(queueTicketStore.countWaiting(10L)).thenReturn(1L);
        when(queueTicketStore.enqueue(10L, 101L, Duration.ofHours(1))).thenReturn(waiting);
        when(queueTicketStore.findWaitingPosition(10L, "qe-wait")).thenReturn(Optional.of(5L));

        JoinQueueUseCase.Output output = joinQueueUseCase.execute(new JoinQueueUseCase.Input(10L, 101L));

        assertThat(output.status()).isEqualTo(QueueEntryStatus.WAITING);
        verify(queueTicketStore).enqueue(10L, 101L, Duration.ofHours(1));
    }

    private ResolvedQueuePolicy createPolicy(final int maxActiveUsers) {
        return new ResolvedQueuePolicy(
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
